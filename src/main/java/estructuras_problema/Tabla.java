package estructuras_problema;

public class Tabla {
    //Esta clase contiene la información de una tabla relevante para el algoritmo.

    private int id;
    private int numFilas; //NumFil
    private int numColumnas; //NumCol
    private int numBytes; //NumByte
    private int[] cardColumnas; //CardTab para una tabla específica

    //Esto indica si un objeto Tabla en realidad representa el resultado de un join.
    private boolean isJoin;

    public Tabla(int id, int numFilas, int numColumnas, int numBytes, int[] cardColumnas) {
        //Se usa para crear un objeto que represente una tabla normal
        this.id = id;
        this.numFilas = numFilas;
        this.numColumnas = numColumnas;
        this.numBytes = numBytes;
        this.cardColumnas = cardColumnas;
        this.isJoin = false;
    }

    public Tabla(int id, int numFilas, int numColumnas, int numBytes, boolean isJoin, int[] cardColumnas) {
        //Se usa para crear un objeto que representa un join
        this.id = id;
        this.numFilas = numFilas;
        this.numColumnas = numColumnas;
        this.numBytes = numBytes;
        this.isJoin = isJoin;
        this.cardColumnas = cardColumnas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumFilas() {
        return numFilas;
    }

    public void setNumFilas(int numFilas) {
        this.numFilas = numFilas;
    }

    public int getNumColumnas() {
        return numColumnas;
    }

    public void setNumColumnas(int numColumnas) {
        this.numColumnas = numColumnas;
    }

    public int getNumBytes() {
        return numBytes;
    }

    public void setNumBytes(int numBytes) {
        this.numBytes = numBytes;
    }

    public boolean getIsJoin() {
        return isJoin;
    }

    public void setIsJoin(boolean isJoin) {
        this.isJoin = isJoin;
    }

    public int[] getCardColumnas() {
        return cardColumnas;
    }

    public void setCardColumnas(int[] cardColumnas) {
        this.cardColumnas = cardColumnas;
    }
}
