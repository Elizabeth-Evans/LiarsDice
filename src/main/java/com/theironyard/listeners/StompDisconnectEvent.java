package com.theironyard.listeners;

import com.theironyard.controllers.LiarsDiceController;
import com.theironyard.dtos.PlayerDto;
import com.theironyard.entities.GameState;
import com.theironyard.entities.Player;
import com.theironyard.services.GameStateRepository;
import com.theironyard.services.PlayerRepository;
import com.theironyard.utils.GameLogic;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.ArrayList;

/**
 * Created by PiratePowWow on 4/11/16.
 */
@Component
public class StompDisconnectEvent implements ApplicationListener<SessionDisconnectEvent> {
    @Autowired
    GameLogic gameLogic;
    @Autowired
    GameStateRepository gameStates;
    @Autowired
    PlayerRepository players;

    private final Log logger = LogFactory.getLog(StompDisconnectEvent.class);

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        GameState gameState = players.findOne(sessionId).getGameState();
        gameLogic.dropPlayer(sessionId);
        ArrayList<PlayerDto> playerDtos = new ArrayList<>();
        for (Player player: players.findByGameStateOrderBySeatNum(gameState)){
            playerDtos.add(new PlayerDto(player.getName(), gameState.getRoomCode(), player.getStake(), player.getSeatNum()));
        }
        LiarsDiceController.messenger.convertAndSend("/topic/playerList", playerDtos);
        System.out.println("Disconnect event [sessionId:" + sessionId + "]");
        logger.debug("Disconnect event [sessionId: " + sessionId + "]");
    }
}
