//stack on mid columns- need to take control for higher chances (horizontal + diagonal likely)
//keep track of moves that are bad? such as invalids or loss moves

public class MyAgent extends Agent {
    /**
     * Constructs a new agent.
     *
     * @param game   the game for the agent to play.
     * @param iAmRed whether the agent is the red player.
     */
    public MyAgent(Connect4Game game, boolean iAmRed) {
        super(game, iAmRed);
    }

    public void move() {
        if(me() != -1){
            moveOnColumn(me());
        }
        else if(them() != -1){
            moveOnColumn(them());
        }
        else{
            boolean[] bad = poorMoves(true);
            boolean[] ok = poorMoves(false);

            int c = 0;
            for(boolean b: bad){
                if(b){
                    c++;
                }
            }
            if(c == myGame.getColumnCount()){
                moveOnColumn(randomMove());
                return;
            }

            for (int i = 0; i < myGame.getColumnCount() / 2; i++) { // ASSUMPTION OF BOARD DIMENSIONS
                int mid = myGame.getColumnCount() / 2;
                if (midStack(ok, mid + i) != -1) {
                    moveOnColumn(mid + i);
                    return;
                }
                if (midStack(ok, mid - i) != -1) {
                    moveOnColumn(mid - i);
                    return;
                }
            }

            for(int i = 0; i < ok.length; i++){
                if(ok[i] && !bad[i]){
                    moveOnColumn(i);
                    return;
                }
            }

            moveOnColumn(randomMove());
        }
    }

    public String getName() {
        return "My Agent";
    }

    public int me(){
        return canWin(iAmRed);
    }

    public int them(){
        return canWin(!iAmRed);
    }

    public void moveOnColumn(int columnNumber) {
        int lowestEmptySlotIndex = getLowestEmptyIndex(myGame.getColumn(columnNumber));
        if (lowestEmptySlotIndex > -1) {
            Connect4Slot lowestEmptySlot = myGame.getColumn(columnNumber).getSlot(lowestEmptySlotIndex);
            if (iAmRed) {
                lowestEmptySlot.addRed();
            } else {
                lowestEmptySlot.addYellow();
            }
        }
    }


    public int getLowestEmptyIndex(Connect4Column column) {
        int lowestEmptySlot = -1;
        for (int i = 0; i < column.getRowCount(); i++) {
            if (!column.getSlot(i).getIsFilled()) {
                lowestEmptySlot = i;
            }
        }
        return lowestEmptySlot;
    }

    public int randomMove() {
        for (int i = 0; i < myGame.getColumnCount(); i++) {
            if (!myGame.getColumn(i).getIsFull()) {
                return i;
            }
        }
        return -1;
    }

    public int canWin(boolean check){
        //cols
        //can next move lead to a vertical win?
        for(int i = 0; i < myGame.getColumnCount(); i++){
            if(vertCheck(i) == 3 && !myGame.getColumn(i).getIsFull()){
                return i;
            }
        }

        //rows
        //can next move lead to a horizontal win?
        for (int i = myGame.getRowCount() - 1; i >= 0; i--) {
            String[] row = getRow(i);
            if (empty(row)) {
                break;
            }
            int count = rowCount(row, check);
            if (count >= 3) {
                String[] under;
                if (i + 1 == myGame.getRowCount()) {
                    under = new String[0];
                }
                else {
                    under = getRow(i + 1);
                }
                int index = test(row, under, check);
                if (index != -1) {
                    return index;
                }
            }
        }
        //diagonal
        for(int c = 0; c < myGame.getColumnCount(); c++){
            Connect4Column col = myGame.getColumn(c);
            int low = getLowestEmptyIndex(col);
            if(!col.getIsFull()){
                if(countDiagonal(c, low, check, true) >= 3){
                    return c;
                }
                if(countDiagonal(c, low, check, false) >= 3){
                    return c;
                }
            }
        }
        //if no winning moves...
        return -1;
    }

    public boolean nextCheck(int col, boolean check){
        Connect4Column c = myGame.getColumn(col);
        if(c.getIsFull()){
            return false;
        }
        int low = getLowestEmptyIndex(c);

        if(low >= 0){
            String[] row = getRow(low);
            if(!(empty(row))){
                int count = 0;

                for(int cfLeft = col - 1; cfLeft >= 0; cfLeft--){
                    Connect4Slot s = myGame.getColumn(cfLeft).getSlot(low);
                    if(s.getIsFilled() && s.getIsRed() == check){
                        count++;
                    }
                    else{
                        break;
                    }
                }
                for(int cfRight = col + 1; cfRight < myGame.getColumnCount(); cfRight++){
                    Connect4Slot s = myGame.getColumn(cfRight).getSlot(low);
                    if(s.getIsFilled() && s.getIsRed() == check){
                        count++;
                    }
                    else{
                        break;
                    }
                }
                if(count >= 3){
                    return true;
                }
            }
        }
        if(countDiagonal(col, low, check, true) >= 3){
            return true;
        }
        else{
            return countDiagonal(col, low, check, false) >= 3;
        }
    }

    public int test(String[] row, String[] under, boolean c){
        for(int i = 0; i < row.length; i++){
            if(row[i].equals("NA")){
                if(under.length != 0){
                    if(under[i].equals("NA")){
                        break;
                    }
                }
                int count = 0;
                String spotC;
                String[] temp = new String[row.length];
                System.arraycopy(row, 0, temp, 0, row.length);
                if(c){
                    spotC = "R";
                }
                else{
                    spotC = "Y";
                }
                temp[i] = spotC;
                for(String s: temp){
                    if(s.equals(spotC)){
                        count++;
                    }
                    else{
                        count = 0;
                    }

                    if(count == 4){
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    //column
    public int vertCheck(int c){
        Connect4Column col = myGame.getColumn(c);
        int count = 0;
        for(int r = getLowestEmptyIndex(col) + 1; r < myGame.getRowCount(); r++){
            Connect4Slot slot = col.getSlot(r);
            boolean top = col.getSlot(getLowestEmptyIndex(col) + 1).getIsRed();
            if(slot.getIsRed() == top) {
                count++;
                if (count == 3) {
                    return 3;
                }
            }
            else{
                return count;
            }
        }
        return 0;
    }

    //row

    public String[] getRow(int r){
        String[] row = new String[myGame.getColumnCount()];
        for(int i = 0; i < myGame.getColumnCount(); i++){
            String spotC = "";
            Connect4Column c = myGame.getColumn(i);
            if(c.getSlot(r).getIsFilled()){
                if(c.getSlot(r).getIsRed()){
                    spotC = "R";
                }
                else{
                    spotC = "Y";
                }
            }
            else{
                spotC = "NA";
            }
            row[i] = spotC;
        }
        return row;
    }

    public boolean empty(String[] row){
        for(String s: row){
            if(!(s.equals("NA"))){
                return false;
            }
        }
        return true;
    }

    public int rowCount(String[] row, boolean c){
        int count = 0;
        for(String s: row){
            if(s.equals("R") && c){
                count++;
            }
            else if(s.equals("Y") && !(c)){
                count++;
            }
        }
        return count;
    }

    //diagonal

//    public int countDiagonal(int low, int col, boolean check, boolean p){
//        int count = 0;
//        int row = 0;
//
//        // l
//        if(p){
//            row = low - 1;
//        }
//        else{
//            row = low + 1;
//        }
//        for(int i = col - 1; i >= 0; i--){
//            if(p){
//                if(row >= 0){
//                    Connect4Slot slot = myGame.getColumn(i).getSlot(row--);
//                    if(slot.getIsFilled() && slot.getIsRed() == check){
//                        count++;
//                    }
//                    else{
//                        break;
//                    }
//                }
//            }
//            else{
//                if(row < myGame.getRowCount()){
//                    Connect4Slot slot = myGame.getColumn(i).getSlot(row++);
//                    if(slot.getIsFilled() && slot.getIsRed() == check){
//                        count++;
//                    }
//                    else{
//                        break;
//                    }
//                }
//            }
//        }
//
//        // r
//        if(p) {
//            row = low + 1;
//        }
//        else{
//            row = low - 1;
//        }
//
//        for(int i = col + 1; i < myGame.getColumnCount(); i++){
//            if(p){
//                if(row < myGame.getRowCount()){
//                    Connect4Slot slot = myGame.getColumn(i).getSlot(row++);
//                    if(slot.getIsFilled() && slot.getIsRed() == check){
//                        count++;
//                    }
//                    else{
//                        break;
//                    }
//                }
//            }
//            else{
//                if(row >= 0){
//                    Connect4Slot slot = myGame.getColumn(i).getSlot(row--);
//                    if(slot.getIsFilled() && slot.getIsRed() == check){
//                        count++;
//                    }
//                    else{
//                        break;
//                    }
//                }
//            }
//        }
//        return count;
//    }

    public int countDiagonal(int column, int lowest, boolean check, boolean p) {
        int count = 0;
        int row = 0;

        if(p){
            row = lowest - 1;
        }
        else{
            row = lowest + 1;
        }
        for (int i = column - 1; i >= 0; i--) {
            if (p? row >= 0:row < myGame.getRowCount()) {
                Connect4Slot slot = myGame.getColumn(i).getSlot(p? row--:row++);
                if (slot.getIsFilled() && slot.getIsRed() == check) {
                    count++;
                }
                else {
                    break;
                }
            }
        }

        if(p){
            row = lowest + 1;
        }
        else{
            row = lowest - 1;
        }
        for (int i = column + 1; i < myGame.getColumnCount(); i++) {
            if (p? row < myGame.getRowCount():row >= 0) {
                Connect4Slot slot = myGame.getColumn(i).getSlot(p? row++:row--);
                if (slot.getIsFilled() && slot.getIsRed() == check) {
                    count++;
                }
                else {
                    break;
                }
            }
        }
        return count;
    }
    public boolean[] poorMoves(boolean e){
        boolean[] col = new boolean[myGame.getColumnCount()];
        for(int i = 0; i < myGame.getColumnCount(); i++){
            if(myGame.getColumn(i).getIsFull()){
                col[i] = true;
            }
        }
        for(int i = 0; i < myGame.getColumnCount(); i++){
            if(!(col[i]) && nextCheck(i, !(iAmRed))){
                col[i] = true;
            }
        }

        if(!e){
            for(int i = 0; i < myGame.getColumnCount(); i++){
                if(!(col[i]) && nextCheck(i, iAmRed)){
                    col[i] = true;
                }
            }
        }
        return col;
    }
    public int midStack(boolean[] ok, int col){
        Connect4Column mid = myGame.getColumn(col);
        if(!(mid.getIsFull()) && !(ok[col])){
            return col;
        }
        return -1;
    }
}
