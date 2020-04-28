package it.polimi.ingsw.psp40.controller;

import it.polimi.ingsw.psp40.commons.Component;
import it.polimi.ingsw.psp40.commons.Configuration;
import it.polimi.ingsw.psp40.commons.messages.Message;
import it.polimi.ingsw.psp40.commons.messages.TypeOfMessage;
import it.polimi.ingsw.psp40.controller.strategies.strategyBuild.StrategyBuild;
import it.polimi.ingsw.psp40.controller.strategies.strategyMove.StrategyMove;
import it.polimi.ingsw.psp40.controller.strategies.strategyWin.StrategyWin;
import it.polimi.ingsw.psp40.exceptions.SantoriniException;
import it.polimi.ingsw.psp40.network.server.VirtualView;
import it.polimi.ingsw.psp40.model.*;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;


/**
 * This class manage all operation of a turn of the current player of the match
 * @author Vito96
 */
public class TurnManager {

    /* Attributes */

    private Match match;
    private VirtualView virtualView;

    private HashMap<String, Turn> turnsMap = new HashMap<>();
    private Turn currentTurn;

    /* Constructor(s) */

    /**
     * Constructor: init turnManager's instance
     * @param match the match of the turn
     */
    public TurnManager(Match match, VirtualView virtualView) {
        this(match);
        this.virtualView = virtualView;
        //richiamo la virtual view per notificargli l'inizio del turno con le fasi disponibili
    }

    /**
     * Constructor for test
     */
    public TurnManager(Match match) {
        this.match = match;
        createTurns();
    }

    /* Methods */
    private void createTurns() {
        for(Player player : match.getPlayers()) {
            Turn turn = new Turn(player);
            buildStrategies(turn);
            turnsMap.put(player.getName(), turn);
        }
        currentTurn = turnsMap.get(match.getCurrentPlayer().getName());
        inizializedCurrentTurn();
    }
    /**
     * This method create strategy instances using reflection
     * @param className the name of the class of the desired istance
     * @param types the class of the params for the costructor of the desired instance
     * @param parameters the parameters of the params for the constructor of the desired instance
     * @return the desidered istance
     * @throws Exception for errors while creating the instance
     */
    private Object createStrategyWithReflection(String className, Class[] types, Object[] parameters) throws Exception {
        Class clazz = Class.forName(className);
        Constructor constructor =  clazz.getConstructor(types);
        return constructor.newInstance(parameters);
    }

    /**
     * This method create set the strategy of the turn from the card of the current player
     */
    private void buildStrategies(Turn turn) {
        //builder/factory
        Card card = turn.getPlayer().getCurrentCard();
        //use reflection
        try {
            //make this for all strategies
            turn.setStrategyMove((StrategyMove) createStrategyWithReflection(Configuration.strategyMovePackage + "." + card.getStrategySettings().getStrategyMove(), new Class[]{Match.class}, new Object[]{match}));
            turn.setStrategyBuild((StrategyBuild) createStrategyWithReflection(Configuration.strategyBuildPackage + "." + card.getStrategySettings().getStrategyBuild(), new Class[]{Match.class}, new Object[]{match}));
            turn.setStrategyWin((StrategyWin) createStrategyWithReflection(Configuration.strategyWinPackage + "." + card.getStrategySettings().getStrategyWin(), new Class[]{Match.class}, new Object[]{match}));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAvailableCellForMove() {
        List<Cell> availableCell = null;
        if (checkForPermittedPhase("move")) {
            availableCell = currentTurn.getAvailableCellForMove();
        }
        // todo chiamare la virtual view con le celle disponibili
    }


    public void move(Cell cell) throws SantoriniException {
        if (checkForPermittedPhase("move")) {
            currentTurn.move(cell);
            updateCurrentPhase("move");
        }
    }

    public void getAvailableCellForBuild() {
        List<Cell> availableCell = null;
        if (checkForPermittedPhase("build")) {
            availableCell = currentTurn.getAvailableCellForBuild();
        }
        // todo chiamare la virtual view con le celle disponibili
    }

    public void build(Component component, Cell cell) throws SantoriniException {
        if (checkForPermittedPhase("build")) {
            currentTurn.build(component, cell);
            updateCurrentPhase("build");
        }
    }

    public void checkWin() {
        if (currentTurn.checkWin()) {
            // todo chiamare la view
        }
    }

    private Boolean checkForPermittedPhase(String type) {
        return currentTurn.getCurrentPhase().getNextPhases().stream().anyMatch(phase -> phase.getType().equals(type));
    }

    public List<Phase> getNextPhases() {
        return currentTurn.getCurrentPhase().getNextPhases();
    }

    public void selectWorker(Worker worker) {
        if (currentTurn.getCurrentPhase().getType().equals("selectWorker")) currentTurn.setSelectedWorker(worker);
    }

    private void updateCurrentPhase(String type) {
        if (currentTurn.getCurrentPhase().getNeedCheckForVictory() && currentTurn.checkWin()) {
            //richiamare la virtualview notificando la vittoria
        }

        currentTurn.updateCurrentPhaseFromType(type);
        if (currentTurn.getCurrentPhase().getNextPhases() == null) {
            //seleziono il prossimo turno
            selectNextTurn();
            //richiamare la virtual view per notificare la fine del turno
        }
    }

    private void selectNextTurn() {
        match.selectNextCurrentPlayer();
        currentTurn = turnsMap.get(match.getCurrentPlayer().getName());
    }

    private void inizializedCurrentTurn() {
        TurnProperties.resetAllParameter();

        //se non ci sono celle disponibili
        if (currentTurn.noAvailableCellForWorkers()) {
            // todo ricordarsi di gestire atena
            String name = this.match.getCurrentPlayer().getName();
            turnsMap.remove(name);
            match.removePlayer(name);
            //notificare la perdità
            selectNextTurn();
        } else {
            this.match.getCurrentPlayer().getWorkers().forEach(worker -> {
                TurnProperties.getInitialPositionMap().put(worker, this.match.getLocation().getLocation(worker));
                TurnProperties.getInitialLevels().put(worker, this.match.getLocation().getLocation(worker).getTower().getTopComponent().getComponentCode());
            });
            updateVirtualView(new Message(currentTurn.getPlayer().getName(), TypeOfMessage.INIT_TURN));
        }
    }

    public Player getCurrentPlayer() {
        return currentTurn.getPlayer();
    }

    private void updateVirtualView(Message message) {
        if (virtualView != null) virtualView.displayMessage(message);
    }

}