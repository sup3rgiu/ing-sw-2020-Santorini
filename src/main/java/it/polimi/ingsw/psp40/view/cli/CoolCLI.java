package it.polimi.ingsw.psp40.view.cli;

import it.polimi.ingsw.psp40.commons.Colors;
import it.polimi.ingsw.psp40.commons.Component;
import it.polimi.ingsw.psp40.commons.Configuration;
import it.polimi.ingsw.psp40.commons.messages.*;
import it.polimi.ingsw.psp40.controller.Phase;
import it.polimi.ingsw.psp40.exceptions.BuildLowerComponentException;
import it.polimi.ingsw.psp40.exceptions.CellOutOfBoundsException;
import it.polimi.ingsw.psp40.model.*;
import it.polimi.ingsw.psp40.network.client.Client;
import it.polimi.ingsw.psp40.view.ViewInterface;
import jdk.jshell.execution.Util;

import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CoolCLI implements ViewInterface {
    private final Client client;
    private static PrintWriter out = new PrintWriter(System.out, true);
    private static Scanner in = new Scanner(System.in);
    Colors colorWorker;

    private Date date = null;
    private int numOfPlayers = 0;


    private static final int MIN_PORT = 1000; // todo usare quelli del server. Possibile?
    private static final int MAX_PORT = 50000;
    private static final int ROWS = 50;
    private static final int COLS = 150;

    private final Utils utils = new Utils(in, out);
    private static Frame upper = new Frame(new int[]{0,0}, new int[]{8, COLS}, in, out);
    private static Frame center = new Frame(new int[]{10,0}, new int[]{ROWS, COLS}, in, out);
    private static Frame center2 = new Frame(new int[]{15,0}, new int[]{ROWS, COLS}, in, out);
    private static Frame lower = new Frame (new int[]{ROWS - 4 ,0}, new int[]{ROWS, COLS}, in, out);

    private static Frame left = new Frame(new int[]{10,0}, new int[]{99, 58}, in, out);

    private boolean fastboot = Configuration.DEBUG;


    private IslandAdapter myisland;


    /**
     * Constructor
     * @param client
     */
    public CoolCLI(Client client) {
        this.client = client;
        Terminal.resize(ROWS, COLS);

        Terminal.superClear();
        Terminal.clearAll();
        Terminal.clearScreen();
        center.clear();


        try {
            maketitle();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }



    @Override
    public void displaySetup() {
        int port = 0;
        String ip ;
        center.clear();

        if(!fastboot) {

            center.center(utils.form("enter address of server", 30), 0);
            Terminal.moveRelativeCursor(-1, -29);

            ip = in.nextLine();

            while (!Utils.isValidIp(ip)) {
                lower.print("This is not a valid IPv4 address. Please, try again:");
                center.center(utils.form("enter address of server", 30), 0);
                Terminal.moveRelativeCursor(-1, -29);
                ip = in.nextLine();
            }

            lower.clear();

            center2.center(utils.form("enter port number", 30), 0);
            Terminal.moveRelativeCursor(-1, -29);

            try {
                port = in.nextInt();
            } catch (InputMismatchException e) {
                center2.center(utils.form("enter port number", 30), 0);
                Terminal.moveRelativeCursor(-1, -29);
                in.nextLine();
            }

            while (port < MIN_PORT || port > MAX_PORT) {
                lower.print("Value must be between " + MIN_PORT + " and " + MAX_PORT + ". Please, try again:");
                center2.center(utils.form("enter port number", 30), 0);
                Terminal.moveRelativeCursor(-1, -29);
                try {
                    port = in.nextInt();
                } catch (InputMismatchException e) {
                    center2.center(utils.form("enter port number", 30), 0);
                    Terminal.moveRelativeCursor(-1, -29);
                    in.nextLine();
                }
            }

        }
        else{
            ip = "localhost";
            port = 1234;
            out.println("DEBUG server localhost:1234");
        }

        client.setServerIP(ip);
        client.setServerPort(port);
        client.connectToServer();
    }

    @Override
    public void displaySetupFailure() {
        left.clear();
        out.println("Can not reach the server, please try again");
        displaySetup();
    }

    @Override
    public void displayLogin() {
        String username;

        if(!fastboot) {
            center.clear();
            center2.clear();

            left.clear();//importante sia l'ultimo clear

            out.println("Choose your username:   ");
            in.nextLine();
             username = utils.readnames();

            date = utils.readDate("birthdate");

            out.println("How many people do you want to play with?");
            numOfPlayers = utils.readNumbers(2, 3);


        }
        else {
            username = new Date().toString();
            DateFormat dateFormat = new SimpleDateFormat(Configuration.formatDate);
            try {
                date =  dateFormat.parse("01/01/1900");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            numOfPlayers = 2;
        }

        client.setUsername(username);
        LoginMessage loginMessage = new LoginMessage(username, date, numOfPlayers, TypeOfMessage.LOGIN);
        client.sendToServer(loginMessage);

    }

    @Override
    public void displayLoginSuccessful() {
        left.clear();
        left.println("You have been logged in successfully");
    }

    @Override
    public void displayLoginFailure(String details) {

    }


    @Override
    public void displayUserJoined(String details) {

        left.clear();
        out.println(details);
    }

    @Override
    public void displayAddedToQueue(String details) {
        left.clear();
        out.println(details);
    }

    @Override
    public void displayStartingMatch() {

        left.clear();
        left.println("MATCH IS STARTING!!!!");
    }

    @Override
    public void displayGenericMessage(String message) {

        left.clear();
        lower.println(message);
    }

    @Override
    public void displayDisconnected(String details) {
        out.println(details);
        client.close();
    }

    @Override
    public void displayCardSelection(HashMap<Integer, Card> cards, int numPlayers) {
        List<Integer> listOfIdCardSelected = new ArrayList<>();
        center.clear();
        center2.clear();
        lower.clear();

        left.clear();//importante sia ultimo
        String[] names = cards.values().stream().map(Card::getName).toArray(String[]::new);

        try {
            utils.singleTableCool("Cards Available", names, 100);
        } catch (InterruptedException e) {
            //todo aggiunere frame per le eccezioni
        }
        System.out.println("Select " + numPlayers + " cards");

        //String[] selectedCards = IntStream.range(0, numPlayers).mapToObj(i -> names[utils.readNumbers(0, names.length - 1)]).toArray(String[]::new);
        List<Integer> selections = utils.readNotSameNumbers(0, names.length - 1, numPlayers );


        for (Integer selection : selections) {
            String nameSelected = names[selection];
            for (HashMap.Entry<Integer, Card> entry : cards.entrySet()) {
                if (nameSelected.equals(entry.getValue().getName())) {
                    listOfIdCardSelected.add(entry.getKey());
                }
            }
        }
        client.sendToServer(new Message( TypeOfMessage.SET_CARDS_TO_GAME, listOfIdCardSelected));

    }

    @Override
    public void displayChoicePersonalCard(List<Card> availableCards) {
        left.clear();
        String[] names = availableCards.stream().map(Card::getName).toArray(String[]::new);

        try {
            utils.singleTableCool("Cards Available", names, 100);
        } catch (InterruptedException e) {
           //todo frame
        }

        System.out.println("Choose your personal card");

        int numberSelected = utils.readNumbers(0, names.length - 1);
        int cardIdSelected = -1;
        for (Card card : availableCards) {
            if (card.getName().equals(names[numberSelected])) {
                cardIdSelected = card.getId();
            }
        }
        client.sendToServer(new Message(TypeOfMessage.SET_CARD_TO_PLAYER, cardIdSelected));
        left.clear();
    }

    @Override
    public void displayCardInGame(List<Card> cardInGame) {
        //todo frame fisso?
        left.clear();
        String[] names = cardInGame.stream().map(Card::getName).toArray(String[]::new);

        try {
            utils.singleTableCool("Card selected", names, 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        left.clear();
    }

    @Override
    public void displaySetInitialPosition(List<Player> playerList) {
        left.clear();

        List<String> colorAlreadyUsed = playerList.stream().flatMap(player -> player.getWorkers().stream()).map(worker -> worker.getColor().toString()).distinct().collect(Collectors.toList());
        List<String> colorsAvailable = Arrays.asList(Colors.allNames()).stream().filter(colorAvailable -> colorAlreadyUsed.indexOf(colorAvailable) == -1).collect(Collectors.toList());

        String[] colorsAvailableArray = colorsAvailable.toArray(new String[0]);//conversion to string
        left.println("I's time to choose one color for your workers, \n choose from following list:");
        try {
            utils.singleTableCool("options", colorsAvailableArray, 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int choice = utils.readNumbers(0,colorsAvailableArray.length - 1);
        colorWorker = Colors.valueOf(colorsAvailableArray[choice]);
        out.println("Wooow, you have selected color " + colorWorker.getAnsiCode() + colorWorker.name() +  Colors.reset() + " for your workers");
        client.sendToServer(new Message(TypeOfMessage.SET_WORKERS_COLOR, colorWorker));


        //START ISLAND

        left.clear();

        try {
            Terminal.noBuffer();
            this.updateIsland();
            myisland.print();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        List<int[]> occupy = cellAdapter(client.getLocationCache().getAllOccupied()) ;

        left.printWrapped("Use arrow keys to select where you want to position your worker, ehen selected press ENTER");
        int[] work1 =position(occupy);

        left.clear();
        left.printWrapped("position your second worker with arrows, \n when done, press q");

        occupy.add(work1);
        int[] work2 = position(occupy);
        left.clear();

        List<CoordinatesMessage> workercord = new ArrayList<>();

        workercord.add(new CoordinatesMessage(work1[0], work1[1]));
        workercord.add(new CoordinatesMessage(work2[0], work2[1]));

        client.sendToServer(new Message(TypeOfMessage.SET_POSITION_OF_WORKER, new SelectWorkersMessage(Colors.valueOf(colorsAvailableArray[choice]), workercord)) );
        //out.println("done");


    }

    @Override
    public void displayAskFirstPlayer(List<Player> allPlayers) {
        left.clear();
        String[] names = allPlayers.stream().map(Player::getName).toArray(String[]::new);

        try {
            utils.singleTableCool("Players available", names, 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //todo double column for also card display

        int selection = utils.readNumbers(0, names.length -1);
        client.sendToServer(new Message(TypeOfMessage.SET_FIRST_PLAYER, names[selection]));

    }

    @Override
    public void displayChoiceOfAvailablePhases(List<Phase> phaseList) {
        left.clear();
        try {
            Terminal.yesBuffer();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Phase selectedPhase = null;
        if (phaseList.size() == 1) {
            selectedPhase = phaseList.get(0);
            out.println("there is only available this phase: " + selectedPhase.getType().toString());
        } else {
            String[] phases = phaseList.stream().map(phase -> phase.getType().toString()).toArray(String[]::new);

            try {
                utils.singleTableCool("Phases available", phases, 100 );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int index = utils.readNumbers(0, phaseList.size());
            selectedPhase = phaseList.get(index);
        }

        switch (selectedPhase.getType()) {
            case SELECT_WORKER:
                displayChoiceSelectionOfWorker();
                break;
            case MOVE_WORKER:
                client.sendToServer(new Message(TypeOfMessage.RETRIEVE_CELL_FOR_MOVE));
                break;
            case BUILD_COMPONENT:
                client.sendToServer(new Message(TypeOfMessage.RETRIEVE_CELL_FOR_BUILD));
                break;
        }

    }

    @Override
    public void displayChoiceOfAvailableCellForMove() {
        List<Cell> availableCells = client.getAvailableMoveCells();

        left.clear();
        try {
            updateIsland();
           // myisland.clearMovable();
            myisland.setMovable(client.getAvailableMoveCells());
            myisland.print();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


        List<int[]> availableCellsCoord = cellAdapter(availableCells) ;
        left.println("These are the cells available for move");
        availableCellsCoord.forEach(cell ->  out.println(cell[0] + "," + cell[1]));

        displayMoveWorker();
    }

    /*
        @Override
        public void displayChoiceOfAvailablePhases(List<Phase> phaseList) {
            Phase selectedPhase = null;
            if (phaseList.size() == 1) {
                selectedPhase = phaseList.get(0);
            } else {
                //logica per far seleziona all'utente la fase fra quelle disponibili
            }

            switch (selectedPhase.getType()) {
                case SELECT_WORKER:
                    displayChoiceSelectionOfWorker();
                    break;
            }
        }
    */

    @Override
    public void displayChoiceSelectionOfWorker() {
        try {
            this.updateIsland();
            myisland.print();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Terminal.yesBuffer();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        left.println("Choose worker selecting the id:");

        getMyWorkers().entrySet().forEach(entry -> {
            out.println("id: " + entry.getKey() + ", coordinates: " + entry.getValue()[0] + "," + entry.getValue()[1]);
        });

        int id = utils.readNumbers(0, getMyWorkers().size() -1);

        client.sendToServer(new Message(TypeOfMessage.SELECT_WORKER, id));
    }
    @Override
    public void displayMoveWorker() {

        position(cellAdapter(client.getAvailableMoveCells()));

    }

    @Override
    public void displayBuildBlock() {

    }

    @Override
    public void displayChoiceOfAvailableCellForBuild() {

    }



    public void updateIsland() throws IOException, InterruptedException {
        myisland = new IslandAdapter(client.getFieldCache(), client.getLocationCache() );

    }


    private int[] position(List<int[]> occupied ){
        boolean debug = false;
        int curRow = 0;
        int curCol = 0;

        while (true) {
            try {
                if (System.in.available() != 0) {
                    int c = System.in.read();  //read one char at a time in ascii code

                    //GETTING P to positiom
                    if (c == 112) {   // if press P -> quit this visualization
                        if (!contains(occupied, curRow, curCol)) {
                            myisland.setWorker(curRow, curCol, colorWorker);
                            myisland.clearSelected();
                            myisland.print();
                            break;
                        }
                    }

                    //getting an ARROW KEY
                    else if (c == 27) { // first part of arrow key == ESC
                        int next1 = System.in.read();
                        int next2 = System.in.read();
                        if (next1 == 91) { //  read [
                            if (next2 == 65) {                     //UP  arrow
                                if (curRow > 0 && curRow <= 5) {
                                    curRow--;
                                }
                            } else if (next2 == 66) {              //DOWN arrow
                                if (curRow >= 0 && curRow < 4) {
                                    curRow++;
                                }
                            } else if (next2 == 67) {              //RIGHT arrow
                                if (curCol >= 0 && curCol < 4) {
                                    curCol++;
                                }
                            } else if (next2 == 68) {               //LEFT  arrow
                                if (curCol > 0 && curCol <= 5) {
                                    curCol--;
                                }
                            }
                        }
                        myisland.setSelected(curRow, curCol);
                        myisland.print();
                    }//end arrow management


                    // gettind D for debug option
                    else if (c == 100) {
                        debug = !debug;
                        if (!debug) {
                            myisland.print();
                        }
                    }

                    if (debug) {
                        myisland.debug();
                    }

                } //end system in available
            } catch (IOException | InterruptedException e) {
                //todo frame per except
            }
        }// end while true



        return new int[]{curRow, curCol};
    }

    private int[] positionAllowed(List<int[]> allowed ){
        boolean debug = false;
        int curRow = 0;
        int curCol = 0;

        while (true) {
            try {
                if (System.in.available() != 0) {
                    int c = System.in.read();  //read one char at a time in ascii code

                    //GETTING P to positiom
                    if (c == 112) {   // if press P -> quit this visualization
                            myisland.setWorker(curRow, curCol, colorWorker);
                            myisland.clearSelected();
                            myisland.print();
                            break;

                    }

                    //getting an ARROW KEY
                    else if (c == 27) { // first part of arrow key == ESC
                        int next1 = System.in.read();
                        int next2 = System.in.read();
                        if (next1 == 91) { //  read [
                            if (next2 == 65) {                     //UP  arrow
                                if (contains(allowed, curRow - 1, curCol)) {
                                    curRow--;
                                }
                            } else if (next2 == 66) {              //DOWN arrow
                                if (contains(allowed, curRow + 1, curCol)) {
                                    curRow++;
                                }
                            } else if (next2 == 67) {              //RIGHT arrow
                                if (contains(allowed, curRow , curCol + 1)) {
                                    curCol++;
                                }
                            } else if (next2 == 68) {               //LEFT  arrow
                                if (contains(allowed, curRow, curCol- 1 )) {
                                    curCol--;
                                }
                            }
                        }
                        myisland.setSelected(curRow, curCol);
                        myisland.print();
                    }//end arrow management


                    // gettind D for debug option
                    else if (c == 100) {
                        debug = !debug;
                        if (!debug) {
                            myisland.print();
                        }
                    }

                    if (debug) {
                        myisland.debug();
                    }

                } //end system in available
            } catch (IOException | InterruptedException e) {
                //todo frame per except
            }
        }// end while true



        return new int[]{curRow, curCol};
    }






    private void maketitle() throws InterruptedException {
        int DELAY;
        if(!fastboot) {
            DELAY = 200;
        }
        else {
            DELAY = 0;
        }
        Terminal.hideCursor();
        String welcome =
                        "oooo     oooo ooooooooooo ooooo         oooooooo8   ooooooo  oooo     oooo ooooooooooo\n" +
                        " 88   88  88   888    88   888        o888     88 o888   888o 8888o   888   888    88 \n" +
                        "  88 888 88    888ooo8     888        888         888     888 88 888o8 88   888ooo8   \n" +
                        "   888 888     888    oo   888      o 888o     oo 888o   o888 88  888  88   888    oo \n" +
                        "    8   8     o888ooo8888 o888ooooo88  888oooo88    88ooo88  o88o  8  o88o o888ooo8888" ;

        String to =
                "ooooooooooo   ooooooo  \n" +
                "88  888  88 o888   888o\n" +
                "    888     888     888\n" +
                "    888     888o   o888\n" +
                "   o888o      88ooo88  " ;
        String santorini =
                        " oooooooo8      o      oooo   oooo ooooooooooo   ooooooo  oooooooooo  ooooo oooo   oooo ooooo\n" +
                        "888            888      8888o  88  88  888  88 o888   888o 888    888  888   8888o  88   888 \n" +
                        " 888oooooo    8  88     88 888o88      888     888     888 888oooo88   888   88 888o88   888 \n" +
                        "        888  8oooo88    88   8888      888     888o   o888 888  88o    888   88   8888   888 \n" +
                        "o88oooo888 o88o  o888o o88o    88     o888o      88ooo88  o888o  88o8 o888o o88o    88  o888o" ;

        upper.clear();
        upper.center(welcome, DELAY);
        TimeUnit.MILLISECONDS.sleep(DELAY);
        upper.clear();
        upper.center(to, DELAY);
        TimeUnit.MILLISECONDS.sleep(DELAY);
        upper.clear();
        upper.center(santorini, DELAY);
        TimeUnit.MILLISECONDS.sleep(DELAY);
        Terminal.showCursor();
    }


    String readIpNew() {
        String ip;
        ip = in.nextLine();

        while (!Utils.isValidIp(ip)) {
            lower.print("This is not a valid IPv4 address. Please, try again:");
            ip = in.nextLine();
        }
        return ip;
    }



    private List<int[]> cellAdapter(List<Cell> cellList){
        List<int[]> coord = new ArrayList<>();
        if(cellList.size() != 0) {
            coord = cellList.stream().map(Cell::getCoordXY).collect(Collectors.toList());
        }
        return coord;
    }

    private HashMap<Integer, Integer[]> getMyWorkers() {
        Location location = client.getLocationCache();
        Cell[][] field = client.getFieldCache();

        HashMap<Integer, Integer[]> workerInfo = new HashMap<>();

        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field.length; j++) {
                Worker occupant = location.getOccupant(i, j);
                if (occupant != null && occupant.getPlayerName().equals(client.getUsername())){
                    workerInfo.put(occupant.getId(), new Integer[]{i, j});
                }
            }
        }
        return workerInfo;
    }

    private boolean contains (List<int[]> lista, int[] candidate){
        boolean bool = false;
        for (int[] ints : lista) {
            if (ints[0] == candidate[0] && ints[1] == candidate[1]) {
                bool = true;
                break;
            }
        }

        return bool;
    }


    private boolean contains (List<int[]> lista, int x, int y){
        boolean bool = false;
        for (int[] ints : lista) {
            if (ints[0] == x && ints[1] == y) {
                bool = true;
                break;
            }
        }

        return bool;
    }


}



