package datos_param;

public class ParametrosAlgoritmo {
    private final int numIter;
    private final int tamPob;
    private final float porcCromCruzados;
    private final float probMut;
    private final float probBusq;
    private final float porcHijosIngresados; //Porcentaje de hijos a seleccionar para entrar en la población
    private final int cantVecinosEvaluados;
    private final float porcIterEstanc; //Porcentaje de iteraciones totales para las que se considera que existe estancamiento

    public ParametrosAlgoritmo(int numIter, int tamPob, float porcCromCruzados, float probMut, float probBusq, float porcHijosSust, int cantVecinosEval, float porcIterEstanc) {
        this.numIter = numIter;
        this.tamPob = tamPob;
        this.porcCromCruzados = porcCromCruzados;
        this.probMut = probMut;
        this.probBusq = probBusq;
        this.porcHijosIngresados = porcHijosSust;
        this.cantVecinosEvaluados = cantVecinosEval;
        this.porcIterEstanc = porcIterEstanc;
    }

    public int getNumIter() {
        return numIter;
    }

    public int getTamPob() {
        return tamPob;
    }

    public float getPorcCromCruzados() {
        return porcCromCruzados;
    }

    public float getProbMut() {
        return probMut;
    }

    public float getProbBusq() {
        return probBusq;
    }

    public float getPorcHijosIngresados() {
        return porcHijosIngresados;
    }

    public int getCantVecinosEvaluados() {
        return cantVecinosEvaluados;
    }

    public float getPorcIterEstanc() {
        return porcIterEstanc;
    }

    @Override
    public String toString() {
        return "ParametrosAlgoritmo{" +
                "numIter=" + numIter +
                ", tamPob=" + tamPob +
                ", porcCromCruzados=" + porcCromCruzados +
                ", probMut=" + probMut +
                ", probBusq=" + probBusq +
                ", porcHijosIngresados=" + porcHijosIngresados +
                ", cantVecinosEval=" + cantVecinosEvaluados +
                ", porcIterEstanc=" + porcIterEstanc +
                '}';
    }
}
