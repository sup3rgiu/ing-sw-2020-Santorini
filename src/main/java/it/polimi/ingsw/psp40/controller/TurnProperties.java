package it.polimi.ingsw.psp40.controller;

import it.polimi.ingsw.psp40.model.Cell;
import it.polimi.ingsw.psp40.model.Worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TurnProperties {
    private static int currentTurnId = -1;
    private static HashMap<Worker, Cell> initialPositionMap = new HashMap<>();
    private static HashMap<Worker, Integer> initialLevels = new HashMap<>();
    private static HashMap<Worker, Cell> builtInThisTurn = new HashMap<>();
    private static List<Phase> performedPhases = new ArrayList<>();




    public static void resetAllParameter() {
        currentTurnId = -1;
        initialPositionMap = new HashMap<>();
        initialLevels = new HashMap<>();
        builtInThisTurn = new HashMap<>();
        performedPhases = new ArrayList<>();
    }

    public static HashMap<Worker, Cell> getInitialPositionMap() {
        return initialPositionMap;
    }

    public static HashMap<Worker, Integer> getInitialLevels() {
        return initialLevels;
    }

    public static List<Phase> getPerformedPhases() {
        return performedPhases;
    }

    public static int getCurrentTurnId() {
        return currentTurnId;
    }

    /**
     * This method is used to update the table of the builds done during a turn
     * @param worker is the {@link Worker} that has built
     * @param cell is the {@link Cell} where the worker has built
     */
    public static void builtNow(Worker worker, Cell cell){
        builtInThisTurn.put(worker, cell);
    }

    /**
     * This method is used to get where a Worker has built during the turn
     * @param worker is the {@link Worker} that has built
     * @return the {@link Cell} where the worker has built
     */
    public static Cell getPreviousBuild(Worker worker){
        return builtInThisTurn.get(worker);
    }

}
