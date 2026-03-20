package com.lbbento.toyrobot.engine.di

import com.lbbento.toyrobot.engine.EngineConfig
import com.lbbento.toyrobot.engine.ProcessCommand
import com.lbbento.toyrobot.engine.RobotGameEngineImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class EngineModule {

    @Binds
    abstract fun bindProcessCommand(
        toyRobotProcessorImpl: RobotGameEngineImpl
    ): ProcessCommand

    @Binds
    abstract fun bindEngineConfig(
        toyRobotProcessorImpl: RobotGameEngineImpl
    ): EngineConfig
}
