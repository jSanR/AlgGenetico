package algoritmo;

import estructuras_problema.Cromosoma;

import java.util.Comparator;

public class ComparadorCromosomas implements Comparator<Cromosoma> {

    @Override
    public int compare(Cromosoma o1, Cromosoma o2) {
        return Double.compare(o2.getFitness(), o1.getFitness());
    }
}
