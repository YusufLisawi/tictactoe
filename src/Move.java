import java.io.Serializable;

class Move implements Serializable {
    public int row, col;
    public Move(int row, int col) {
        this.row = row;
        this.col = col;
    };

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Move)) return false;
        Move p = (Move) o;
        return p.row == row && p.col == col;
    }

    @Override
    public String toString() {
        return "Move{" +
                "row=" + row +
                ", col=" + col +
                '}';
    }
}