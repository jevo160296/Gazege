package com.example.gazege.ui.progressStatus

import kotlin.math.min

enum class Status {
    NOT_STARTED,
    STARTED,
    COMPLETED,
    ERROR
}

abstract class IProgressStatus(
    open var message: String,
    open var status: Status
) {
    abstract val progress: Double

    override fun toString(): String {
        return "ProgressStatus(message = $message, progress = $progress)"
    }
}

abstract class IMutableProgressStatus(
    message: String,
    status: Status,
    open val onUpdate: (progressStatus: IMutableProgressStatus) -> Unit
) : IProgressStatus(message, status) {
    open fun error(message: String) = this
        .apply {
            this.status = Status.ERROR
            this.message = message
        }
        .also(onUpdate)
}

open class IncrementalProgressStatus(
    message: String,
    status: Status,
    val totalWork: Double,
    var completedWork: Double,
    val defaultIncrement: Double,
    onUpdate: (progressStatus: IProgressStatus) -> Unit
) :
    IMutableProgressStatus(
        message = message,
        status = status,
        onUpdate = onUpdate
    ) {
    override val progress: Double
        get() = completedWork / totalWork

    fun incrementProgress(
        message: String,
        workDone: Double = defaultIncrement
    ) = setCompletedWork(message, completedWork + workDone)

    fun finish(message: String) = apply {
        setCompletedWork(message, totalWork)
        status = Status.COMPLETED
    }
        .run(onUpdate)

    open fun setCompletedWork(message: String, newCompletedWork: Double) =
        apply {
            this.message = message
            this.completedWork = min(newCompletedWork, totalWork)
        }
            .also(onUpdate)

    companion object {
        fun start(
            message: String,
            totalWork: Double,
            defaultIncrement: Double,
            onUpdate: (progressStatus: IProgressStatus) -> Unit
        ) = IncrementalProgressStatus(
            message = message,
            status = Status.STARTED,
            totalWork = totalWork,
            completedWork = 0.0,
            defaultIncrement = defaultIncrement,
            onUpdate = onUpdate
        )
            .also { onUpdate(it) }
    }
}

class HistoricalProgressStatus(
    message: String,
    status: Status,
    totalWork: Double,
    completedWork: Double,
    defaultIncrement: Double,
    onUpdate: (progressStatus: IProgressStatus) -> Unit
) : IncrementalProgressStatus(
    message,
    status,
    totalWork,
    completedWork,
    defaultIncrement,
    onUpdate
) {
    private val messageHistory = mutableListOf<String>()
    override fun setCompletedWork(
        message: String,
        newCompletedWork: Double
    ) = this.messageHistory
        .apply {
            add(message)
            if (size > 3) {
                this.removeFirst()
            }
        }
        .let {
            super.setCompletedWork(
                it.joinToString("\n"),
                newCompletedWork
            )
        }

    companion object {
        fun start(
            message: String,
            totalWork: Double,
            defaultIncrement: Double,
            onUpdate: (progressStatus: IProgressStatus) -> Unit
        ) = HistoricalProgressStatus(
            message = message,
            status = Status.STARTED,
            totalWork = totalWork,
            completedWork = 0.0,
            defaultIncrement = defaultIncrement,
            onUpdate = onUpdate
        )
            .also { onUpdate(it) }
    }
}