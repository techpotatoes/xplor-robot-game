package com.lbbento.toyrobot.game.di

import com.lbbento.toyrobot.game.data.GameEngineInteractorImpl
import com.lbbento.toyrobot.game.domain.GetGameConfig
import com.lbbento.toyrobot.game.domain.MoveRobot
import com.lbbento.toyrobot.game.domain.PlaceRobot
import com.lbbento.toyrobot.game.domain.TurnLeft
import com.lbbento.toyrobot.game.domain.TurnRight
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class GameModule {

    @Binds
    abstract fun bindPlaceRobot(interactor: GameEngineInteractorImpl): PlaceRobot

    @Binds
    abstract fun bindMoveRobot(interactor: GameEngineInteractorImpl): MoveRobot

    @Binds
    abstract fun bindTurnLeft(interactor: GameEngineInteractorImpl): TurnLeft

    @Binds
    abstract fun bindTurnRight(interactor: GameEngineInteractorImpl): TurnRight

    @Binds
    abstract fun bindGetGameConfig(interactor: GameEngineInteractorImpl): GetGameConfig
}
