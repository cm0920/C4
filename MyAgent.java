public class MyAgent extends Agent {

    public MyAgent(Connect4Game game, boolean iAmRed) {
        super(game, iAmRed);
    }

    public void move() {
        if (iCanWin() != - 1) {
            moveOnColumn(iCanWin());
        }
        else if (theyCanWin() != -1) {
            moveOnColumn(theyCanWin());
        }
        else if (pairCheck() != -1) {
            moveOnColumn(pairCheck());
        }
        else {
            boolean[] blacklist = getBlacklistedColumns(true);
            boolean[] greylist = getBlacklistedColumns(false);

            int count = 0;
            for (boolean b: blacklist) {
                if (b) count++;
            }
            if (count == myGame.getColumnCount()) {
                moveOnColumn(randomMove());
                return;
            }

            for (int i = 0; i < myGame.getColumnCount() / 2; i++) {
                int mid = myGame.getColumnCount() / 2;
                if (stackColumn(greylist, mid + i) != -1) {
                    moveOnColumn(mid + i);
                    return;
                }
                if (stackColumn(greylist, mid - i) != -1) {
                    moveOnColumn(mid - i);
                    return;
                }
            }

            for (int i = 0; i < greylist.length; i++) {
                if (greylist[i] && !blacklist[i]) {
                    moveOnColumn(i);
                    return;
                }
            }


            moveOnColumn(randomMove());
        }
    }


    public void moveOnColumn(int columnNumber) {
        int lowestEmptySlotIndex = getLowestEmptyIndex(myGame.getColumn(columnNumber));
        if (lowestEmptySlotIndex > -1)
        {
            Connect4Slot lowestEmptySlot = myGame.getColumn(columnNumber).getSlot(lowestEmptySlotIndex);
            if (iAmRed)
            {
                lowestEmptySlot.addRed();
            } else
            {
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

    public int iCanWin() {
        return canWin(iAmRed);
    }
    public int theyCanWin() {
        return canWin(!iAmRed);
    }


    public String getName() {
        return "My Agent";
    }

    //

    public int canWin(boolean toCheck) {
        for (int i = 0; i < myGame.getColumnCount(); i++) {
            if (longestRunVertical(i) == 3 && !myGame.getColumn(i).getIsFull()) {
                return i;
            }
        }

        // @TODO fix. literally just use getLowestEmptyIndex. loop through columns instead of rows
        for (int i = myGame.getRowCount() - 1; i >= 0; i--) {
            char[] row = getRow(i);
            if (isEmpty(row)) {
                break;
            }
            int count = countRow(row, toCheck);
            if (count >= 3) {
                char[] rowUnder;
                if (i + 1 == myGame.getRowCount()) {
                    rowUnder = new char[0];
                }
                else {
                    rowUnder = getRow(i + 1);
                }
                int index = testRow(row, rowUnder, toCheck);
                if (index != -1) {
                    return index;
                }
            }
        }

        for (int col = 0; col < myGame.getColumnCount(); col++) {
            Connect4Column c = myGame.getColumn(col);
            int lowest = getLowestEmptyIndex(c);
            if (!c.getIsFull()) {
                if (getDiagonalCount(col, lowest, toCheck, true) >= 3) {
                    return col;
                }
                if (getDiagonalCount(col, lowest, toCheck, false) >= 3) {
                    return col;
                }
            }
        }
        return -1;
    }

    public boolean canWinInTwo(int col, boolean toCheck) {
        Connect4Column c = myGame.getColumn(col);
        if (c.getIsFull()) return false;

        int lowest = getLowestEmptyIndex(c) - 1; // can win is just checking one spot above

        if (lowest >= 0) {
            char[] row = getRow(lowest);
            if (!isEmpty(row)) {
                int count = 0;

                for (int colTraverse = col + 1; colTraverse < myGame.getColumnCount(); colTraverse++) {
                    Connect4Slot slot = myGame.getColumn(colTraverse).getSlot(lowest);
                    if (slot.getIsFilled() && slot.getIsRed() == toCheck) {
                        count++;
                    }
                    else {
                        break;
                    }
                }

                for (int colTraverse = col - 1; colTraverse >= 0; colTraverse--) {
                    Connect4Slot slot = myGame.getColumn(colTraverse).getSlot(lowest);
                    if (slot.getIsFilled() && slot.getIsRed() == toCheck) {
                        count++;
                    }
                    else {
                        break;
                    }
                }
                if (count >= 3) {
                    return true;
                }
            }
        }

        if (getDiagonalCount(col, lowest, toCheck, true) >= 3) {
            return true;
        }
        return getDiagonalCount(col, lowest, toCheck, false) >= 3;
    }

    // vert

    public int longestRunVertical(int col) {
        int count = 0;
        Connect4Column c = myGame.getColumn(col);
        int rowStart = getLowestEmptyIndex(c) + 1;
        for (int row = rowStart; row < myGame.getRowCount(); row++) {
            Connect4Slot slot = c.getSlot(row);
            boolean topSlot = c.getSlot(rowStart).getIsRed();
            if (slot.getIsRed() == topSlot) {
                count++;
                if (count == 3) {
                    return 3;
                }
            }
            else {
                return count;
            }
        }
        return 0;
    }

    // horizontal

    public char[] getRow(int r) {
        char[] row = new char[myGame.getColumnCount()];
        for (int i = 0; i < myGame.getColumnCount(); i++) {
            char status;
            Connect4Column c = myGame.getColumn(i);
            if (c.getSlot(r).getIsFilled()) {
                if (c.getSlot(r).getIsRed()) {
                    status = 'R';
                }
                else {
                    status = 'Y';
                }
            }
            else {
                status = '-';
            }
            row[i] = status;
        }
        return row;
    }

    public int countRow(char[] row, boolean color) {
        int count = 0;
        for (char ch: row) {
            if (ch == 'R' && color) {
                count++;
            }
            else if (ch == 'Y' && !color) {
                count++;
            }
        }
        return count;
    }

    public boolean isEmpty(char[] row) {
        for (char ch: row) {
            if (ch != '-') {
                return false;
            }
        }
        return true;
    }

    public int testRow(char[] row, char[] rowUnder, boolean color) {
        for (int i = 0; i < row.length; i++) {
            if (row[i] == '-') {
                if (rowUnder.length != 0) {
                    if (rowUnder[i] == '-') {
                        break;
                    }
                }
                char[] temp = new char[row.length];
                System.arraycopy(row, 0, temp, 0, row.length);
                char status = color?'R':'Y';
                temp[i] = status;
                int count = 0;
                for (char ch: temp) {
                    if (ch == status) {
                        count++;
                    }
                    else {
                        count = 0;
                    }

                    if (count == 4) return i;
                }
            }
        }
        return -1;
    }

    public int pairCheck() {
        for (int i = myGame.getRowCount() - 1; i >= 0; i--) {
            char[] row = getRow(i);
            if (isEmpty(row)) {
                break;
            }
            int count = countRow(row, !iAmRed);
            if (count < 2) {
                break;
            }

            char[] rowUnder;
            if (i + 1 == myGame.getRowCount()) {
                rowUnder = new char[0];
            }
            else {
                rowUnder = getRow(i + 1);
            }

            boolean[] blacklist = getBlacklistedColumns(true);
            for (int test = 1; test < row.length - 2; test++) {

                char enemy = iAmRed?'Y':'R';
                if (row[test] == enemy && row[test + 1] == enemy && row[test - 1] == '-' && row[test + 2] == '-') {
                    if (rowUnder.length != 0) {
                        if (rowUnder[test - 1] == '-' || rowUnder[test + 2] == '-') {
                            break;
                        }
                    }

                    if (!blacklist[test - 1]) {
                        return test - 1;
                    }
                    else if (!blacklist[test + 2]) {
                        return test + 2;
                    }
                }


                if (test != row.length - 3) {
                    if (row[test] == enemy && row[test + 1] == '-' && row[test + 2] == enemy) {
                        if (!blacklist[test + 1]) {
                            return test + 1;
                        }
                    }
                }
            }
        }

        return -1;
    }

    // diagonal

    public int stackColumn(boolean[] greylist, int column) {
        Connect4Column mid = myGame.getColumn(column);
        if (!mid.getIsFull() && !greylist[column]) {
            return column;
        }
        return -1;
    }
    public int getDiagonalCount(int column, int lowest, boolean toCheck, boolean positive) {
        int count = 0;
        int row;

        if(positive){
            row = lowest + -1;
        }
        else{
            row = lowest + 1;
        }

        for (int i = column - 1; i >= 0; i--) {
            if (positive? row >= 0:row < myGame.getRowCount()) {
                Connect4Slot slot = myGame.getColumn(i).getSlot(positive? row--:row++);
                if (slot.getIsFilled() && slot.getIsRed() == toCheck) {
                    count++;
                }
                else {
                    break;
                }
            }
        }

        if(positive){
            row = lowest + 1;
        }
        else{
            row = lowest + -1;
        }

        for (int i = column + 1; i < myGame.getColumnCount(); i++) {
            if (positive? row < myGame.getRowCount():row >= 0) {
                Connect4Slot slot = myGame.getColumn(i).getSlot(positive? row++:row--);
                if (slot.getIsFilled() && slot.getIsRed() == toCheck) {
                    count++;
                }
                else {
                    break;
                }
            }
        }
        return count;
    }

    // strat

    public boolean[] getBlacklistedColumns(boolean strict) {
        // blacklist
        boolean[] columns = new boolean[myGame.getColumnCount()];
        for (int i = 0; i < myGame.getColumnCount(); i++) {
            if (myGame.getColumn(i).getIsFull()) {
                columns[i] = true;
            }
        }
        for (int i = 0; i < myGame.getColumnCount(); i++) {
            if (!columns[i] && canWinInTwo(i, !iAmRed)) {
                columns[i] = true;
            }
        }
        // greylist
        if (!strict) {
            for (int i = 0; i < myGame.getColumnCount(); i++) {
                if (!columns[i] && canWinInTwo(i, iAmRed)) {
                    columns[i] = true;
                }
            }
        }
        return columns;
    }


}
