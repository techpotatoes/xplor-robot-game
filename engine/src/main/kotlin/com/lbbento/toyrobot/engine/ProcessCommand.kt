package com.lbbento.toyrobot.engine

import com.lbbento.toyrobot.engine.model.Command
import com.lbbento.toyrobot.engine.model.ProcessResult
import com.lbbento.toyrobot.engine.model.RobotPosition

fun interface ProcessCommand {
    fun process(currentPosition: RobotPosition?, command: Command): ProcessResult
}
