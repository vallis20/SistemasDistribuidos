
import java.io.Serializable;
import java.rmi.Remote;
import java.util.Arrays;
import java.util.Objects;

public class Progreso implements Remote, Serializable {

    private static final long serialVersionUID = 1L;
    private final String filename;
    private short pieces[];// 0=no part in stock;1=discharging;2=part discharged
    private int[] connection; // here are stored the IDs of the peers with which you have a connection
    private boolean full = false;// indicates whether the file is complete, in order to avoid comparing one by
                                 // one the between the local client and the remote client

    Progreso(String filename, short[] pieces, int[] connection) {
        this.filename = filename;
        this.pieces = pieces;
        this.connection = connection;
    }

    Progreso(String filename, int totalPieces, int[] connection) {
        this.filename = filename;
        pieces = new short[totalPieces];
        this.connection = connection;
    }

    Progreso(String filename, int totalPieces) {
        this.filename = filename;
    }

    public void setFull() {
        full = true;
    }

    public String getFilename() {
        return filename;
    }

    public int getValue() {// calculates the percentage of progress
        int v = 0;
        for (short piece : pieces) {
            if (piece == 2)
                v++;
        }
        v = 100 * v / pieces.length;
        if (v == 100)
            full = true;
        return v;
    }

    public short[] getPieces() {
        return pieces;
    }

    public int getPiecesLength() {
        return pieces.length;
    }

    public synchronized void setPieces(short[] pieces) {
        this.pieces = pieces;
    }

    public short getPieceValue(int i) throws StackOverflowError {
        return pieces[i];
    }

    public synchronized void setPieceValue(int i, short value) throws StackOverflowError {
        pieces[i] = value;
    }

    public boolean getState() {
        return full;
    }

    public void clear() {
        for (int i = 0; i < pieces.length; i++) {
            if (pieces[i] == 1) {
                pieces[i] = 0;
            }
        }
    }

    public synchronized int getIndexPieceNull(Progreso p) {// obtains the index of the first piece without downloading,
                                                           // under the condition that the remote client has it

        if (p.getState()) {
            for (int i = 0; i < pieces.length; i++) {
                if (pieces[i] == 0) {
                    pieces[i] = 1;
                    return i;
                }
            }
        } else {
            for (int i = 0; i < pieces.length; i++) {
                if (pieces[i] == 0 && getPieceValue(i) == 2) {
                    pieces[i] = 1;
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.filename);
        hash = 89 * hash + Arrays.hashCode(this.pieces);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Progreso other = (Progreso) obj;
        if (!Objects.equals(this.filename, other.filename)) {
            return false;
        }
        if (!Arrays.equals(this.pieces, other.pieces)) {
            return false;
        }
        return true;
    }

}
