package it.polimi.ingsw.controller.strategies.strategyBuild;

import it.polimi.ingsw.commons.Colors;
import it.polimi.ingsw.commons.Component;
import it.polimi.ingsw.controller.TurnProperties;
import it.polimi.ingsw.exceptions.ComponentNotAllowed;
import it.polimi.ingsw.exceptions.SantoriniException;
import it.polimi.ingsw.exceptions.WrongCellSelectedBuildException;
import it.polimi.ingsw.model.Cell;
import it.polimi.ingsw.model.Match;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.Worker;
import org.junit.Before;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;



/*
Initial Map:
╔═════╦═══════════╤═══════════╤═══════════╤═══════════╤═══════════╗
║  4  ║           │           │           │           │           ║
╟─────╫───────────┼───────────┼───────────┼───────────┼───────────╢
║  3  ║           │           │           │ Worker1_2 │           ║
╟─────╫───────────┼───────────┼───────────┼───────────┼───────────╢
║  2  ║           │   LVL1    │ Worker1_1 │           │           ║
╟─────╫───────────┼───────────┼───────────┼───────────┼───────────╢
║  1  ║   DOME    │ Worker2_2 │           │           │           ║
╟─────╫───────────┼───────────┼───────────┼───────────┼───────────╢
║  0  ║ Worker2_1 │   LVL2    │           │           │           ║
╠═════╬═══════════╪═══════════╪═══════════╪═══════════╪═══════════╣
║ y/x ║     0     │     1     │     2     │     3     │     4     ║
╚═════╩═══════════╧═══════════╧═══════════╧═══════════╧═══════════╝
 */

    public class DomeAnywhereTest {
        private Match match;
        private Player player1;
        private Player player2;
        Worker worker1_1;
        Worker worker1_2;
        Worker worker2_1;
        Worker worker2_2;
        Cell initCellWorker1_1;
        Cell initCellWorker1_2;
        Cell initCellWorker2_1;
        Cell initCellWorker2_2;
        StrategyBuild strategyBuild;
        TurnProperties turnProperties;

        @Before
        public void setUp() throws Exception {
            match = new Match(12123131);

            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String birthDate1 = "22/03/1998";
            String birthDate2 = "26/07/1997";
            Date date1 = dateFormat.parse(birthDate1);
            Date date2 = dateFormat.parse(birthDate2);
            player1 = match.createPlayer("Mario", date1);
            player2 = match.createPlayer("Luigi", date2);

            worker1_1 = player1.addWorker(Colors.RED);
            worker1_2 = player1.addWorker(Colors.RED);
            worker2_1 = player2.addWorker(Colors.BLUE);
            worker2_2 = player2.addWorker(Colors.BLUE);

            initCellWorker1_1 = match.getIsland().getCell(2, 2);
            initCellWorker1_2 = match.getIsland().getCell(3, 3);
            initCellWorker2_1 = match.getIsland().getCell(0, 0);
            initCellWorker2_2 = match.getIsland().getCell(1, 1);
            match.getLocation().setLocation(initCellWorker1_1, worker1_1);
            match.getLocation().setLocation(initCellWorker1_2, worker1_2);
            match.getLocation().setLocation(initCellWorker2_1, worker2_1);
            match.getLocation().setLocation(initCellWorker2_2, worker2_2);

            match.getIsland().getCell(0, 1).getTower().addComponent(Component.FIRST_LEVEL);
            match.getIsland().getCell(0, 1).getTower().addComponent(Component.SECOND_LEVEL);
            match.getIsland().getCell(0, 1).getTower().addComponent(Component.THIRD_LEVEL);
            match.getIsland().getCell(0, 1).getTower().addComponent(Component.DOME);

            match.getIsland().getCell(1, 0).getTower().addComponent(Component.FIRST_LEVEL);
            match.getIsland().getCell(1, 0).getTower().addComponent(Component.SECOND_LEVEL);

            match.getIsland().getCell(1, 2).getTower().addComponent(Component.FIRST_LEVEL);

            strategyBuild = new DomeAnywhere(match); //SET HERE STRATEGYY
            turnProperties = new TurnProperties();
            TurnProperties.resetAllParameter();
        }

        @Test
        public void build_levelZero_shouldBeSuccessful() throws SantoriniException {
            Cell cellToBuild1_1 = match.getIsland().getCell(4, 4);
            strategyBuild.build(Component.FIRST_LEVEL, cellToBuild1_1, worker1_2);
            assertEquals(cellToBuild1_1.getTower().getTopComponent(), Component.FIRST_LEVEL);
        }
        @Test
        public void build_DomeAtLevelZero() throws SantoriniException {
            Cell cellToBuild1_1 = match.getIsland().getCell(4, 4);
            strategyBuild.build(Component.DOME, cellToBuild1_1, worker1_2);
            assertEquals(cellToBuild1_1.getTower().getTopComponent(), Component.DOME);
        }

        @Test
        public void build_DomeAtLevelTwo() throws SantoriniException {
            Cell cellToBuild1_1 = match.getIsland().getCell(1, 0);
            strategyBuild.build(Component.DOME, cellToBuild1_1, worker2_2);
            assertEquals(cellToBuild1_1.getTower().getTopComponent(), Component.DOME);
        }


        @Test (expected = ComponentNotAllowed.class)
        public void build_MiddleLevelNotAllowed() throws SantoriniException {
            Cell cellToBuild1 = match.getIsland().getCell(4, 4);
            strategyBuild.build(Component.THIRD_LEVEL, cellToBuild1, worker1_2);

            assertNotEquals(cellToBuild1.getTower().getTopComponent(), Component.THIRD_LEVEL);
        }

        @Test (expected = WrongCellSelectedBuildException.class)
        public void build_DomeOnDomelNotAllowed() throws SantoriniException {
            Cell cellToBuild1 = match.getIsland().getCell(0, 1);
            strategyBuild.build(Component.DOME, cellToBuild1, worker2_1);

            assertEquals(cellToBuild1.getTower().getTopComponent(), Component.DOME);
        }
    }

