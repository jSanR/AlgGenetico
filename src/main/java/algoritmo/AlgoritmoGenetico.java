package algoritmo;

import datos_param.DatosEntrada;
import datos_param.ParametrosAlgoritmo;
import estructuras_problema.Cromosoma;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.SplittableRandom;

public class AlgoritmoGenetico {
    private final int numIter;
    private final int tamPob;
    private final float porcCromCruzados;
    private final int cantCromCruzados;
    private final float probMut;
    private final float porcHijosIngresados; //Porcentaje del total de hijos a ingresar en la población
    private final int cantHijosIngresados;
    private final float porcIterEstancamiento; //Si tiene valor 1 (100%), no se consideraría estancamiento
    private Cromosoma[] poblacion;
    private double fitnessSumadaPoblacion;
    private Cromosoma mejorSolucion;
    private double fitnessMejorSolucion;
    private int indiceMejorSolucion;
    private double tiempoEjecucion; //Tiempo de ejecución en milisegundos
    private double fitnessPromMejores10; //Fitness promedio de las 10 mejores soluciones finales
    private double fitnessPromMejores20; //Fitness promedio de las 20 mejores soluciones finales

    public AlgoritmoGenetico() {
        //Se accede a los datos de entrada para copiar los parámetros de ejecución
        ParametrosAlgoritmo param = DatosEntrada.getInstance(null).getParamAlg()[0];
        this.numIter = param.getNumIter();
        this.tamPob = param.getTamPob();
        this.porcCromCruzados = param.getPorcCromCruzados();
        int cantCruce = (int) (tamPob*porcCromCruzados);
        //Corrección de la cantidad de cromosomas a cruzar (número par que se encuentre dentro del tamPob)
        if(cantCruce%2==1) {
            if(cantCruce + 1 > tamPob) this.cantCromCruzados = cantCruce - 1;
            else this.cantCromCruzados = cantCruce + 1;
        }
        else this.cantCromCruzados = cantCruce;
        if(this.cantCromCruzados<2){
            System.err.println("ERROR: No es posible realizar el cruce con los parámetros especificados (tamPob: " +
                    this.tamPob + ", porcCromCruzados: " + this.porcCromCruzados + ")");
            System.exit(3);
        }
        this.probMut = param.getProbMut();
        this.porcHijosIngresados = param.getPorcHijosIngresados();
        int cantIngreso = (int) ((cantCromCruzados/2)*porcHijosIngresados);
        //Corrección de la cantidad de hijos a ingresar
        if(cantIngreso>cantCromCruzados/2) cantIngreso = cantCromCruzados/2;
        if(cantIngreso<=0){
            System.err.println("ERROR: No es posible realizar la evolución de población con los parámetros especificados " +
                    "(cantCromCruzados: " + this.cantCromCruzados + ", porcHijosIngresados: " + this.porcHijosIngresados + ")");
            System.exit(3);
        }
        this.cantHijosIngresados = cantIngreso;
        this.porcIterEstancamiento = param.getPorcIterEstanc();
    }

    public AlgoritmoGenetico(int numIter, int tamPob, float porcCromCruzados, float probMut, float porcHijosIngresados,
                             float porcIterEstancamiento) {
        this.numIter = numIter;
        this.tamPob = tamPob;
        this.porcCromCruzados = porcCromCruzados;
        int cantCruce = (int) (tamPob*porcCromCruzados);
        //Corrección de la cantidad de cromosomas a cruzar (número par que se encuentre dentro del tamPob)
        if(cantCruce%2==1) {
            if(cantCruce + 1 > tamPob) this.cantCromCruzados = cantCruce - 1;
            else this.cantCromCruzados = cantCruce + 1;
        }
        else this.cantCromCruzados = cantCruce;
        if(this.cantCromCruzados<2){
            System.err.println("ERROR: No es posible realizar el cruce con los parámetros especificados (tamPob: " +
                    this.tamPob + ", porcCromCruzados: " + this.porcCromCruzados + ")");
            System.exit(3);
        }
        this.probMut = probMut;
        this.porcHijosIngresados = porcHijosIngresados;
        int cantIngreso = (int) ((cantCromCruzados/2)*porcHijosIngresados);
        //Corrección de la cantidad de hijos a ingresar
        if(cantIngreso>cantCromCruzados/2) cantIngreso = cantCromCruzados/2;
        if(cantIngreso<=0){
            System.err.println("ERROR: No es posible realizar la evolución de población con los parámetros especificados " +
                    "(cantCromCruzados: " + this.cantCromCruzados + ", porcHijosIngresados: " + this.porcHijosIngresados + ")");
            System.exit(3);
        }
        this.cantHijosIngresados =cantIngreso;
        this.porcIterEstancamiento = porcIterEstancamiento;
    }

    public Cromosoma ejecutar(int verbosityLevel){
        //Verbosity level:
        //<0: Sin mensajes impresos de la ejecución
        //0: Sin mensajes de progreso del algoritmo. Solo el resultado final
        //1: Mensajes de progreso del algoritmo por iteración (no se detalla el avance dentro de cada iteración)
        //2+: Mensajes de progreso del algoritmo completos (incluidos al interior de cada iteración)
        DecimalFormat df1 = new DecimalFormat("0.0000000000000");
        DecimalFormat df2 = new DecimalFormat("0.00000");
        df1.setRoundingMode(RoundingMode.HALF_UP);
        df2.setRoundingMode(RoundingMode.HALF_UP);
        df2.setPositivePrefix("+");
        //Inicialización de la población de acuerdo al tamaño (tamPob)
        if(verbosityLevel>=1) System.out.println("Inicializando población y calculando fitness");
        long tiempoIni = System.nanoTime();
        inicializarPoblacion();
        double fitnessPrimerMejor = this.fitnessMejorSolucion;
        Cromosoma primerMejor = this.mejorSolucion;
        //Fitness sumado de la población anterior a la actual, inicia siendo la de la población original
        double fitnessSumadaAnterior = this.fitnessSumadaPoblacion;
        if(verbosityLevel>=0) System.out.println("Población inicializada. Fitness del mejor cromosoma: " + this.fitnessMejorSolucion);
        if(verbosityLevel>=0) System.out.println("Inicio de optimización...");
        if(verbosityLevel>=2)
            System.out.println("=".repeat(30));
        //Ahora se inician las iteraciones
        //Fuente de aleatoriedad
        SplittableRandom random = new SplittableRandom();
        //Condición de parada 1: estancamiento
        //Se calcula la cantidad de iteraciones seguidas que deben acumularse para considerarse que existe estancamiento
        int cantIterEstanc = (int) Math.ceil(this.numIter*this.porcIterEstancamiento);
        if(cantIterEstanc> this.numIter) cantIterEstanc = this.numIter;
        //Contador de estancamiento
        int contadorEstanc = 0;
        for (int i=1; i<=numIter;i++){ //Condición de parada 2: Límite de iteraciones
            if(verbosityLevel>=2)
                System.out.println("Inicio de iteración " + i + ". Seleccionando padres...");
            //Selección de padres mediante método de ruleta
            Cromosoma[] arrPadres = seleccionXTorneo(random);
            if(verbosityLevel>=2)
                System.out.println(cantCromCruzados + " padres seleccionados");
            //Cruce/casamiento de los padres para producir hijos
            if(verbosityLevel>=2)
                System.out.println("Generando hijo(s) mediante casamiento (Partially Mapped Crossover)");
            Cromosoma[] arrHijos = casamientoPMX(arrPadres, random);
            if(verbosityLevel>=2)
                System.out.println((cantCromCruzados/2) + " hijo(s) generado(s) mediante casamiento");
            //Mutación de los cromosomas hijos de forma probabilística
            if(verbosityLevel>=2)
                System.out.println("Iniciando mutación probabilística de hijos mediante Swap Mutation");
            arrHijos = inversionMutation(arrHijos, verbosityLevel, random);
            //Cálculo de fitness de los hijos (si se aplicó búsqueda local algunos fitness ya deberían estar calculadas)
            if(verbosityLevel>=2)
                System.out.println("Calculando fitness/calidad de los cromosomas hijos");
            for(int j=0; j< arrHijos.length;j++) arrHijos[j].getFitness();
            if(verbosityLevel>=2)
                System.out.println("Fitness calculada. Iniciando evolución de población");
            boolean cambioMejorCrom = evolPoblacion(arrHijos, random);
            if(verbosityLevel>=2)
                System.out.println("Obtenida nueva generación de la población. Se realizaron "
                        + this.cantHijosIngresados + " reemplazos");
            //Generación de un resumen de la iteración
            double porcMejoraFitness = (this.fitnessSumadaPoblacion-fitnessSumadaAnterior)/fitnessSumadaAnterior;
            //Actualizar fitness sumado de la población anterior
            fitnessSumadaAnterior = this.fitnessSumadaPoblacion;
            //Verificar parada por estancamiento
            if(cambioMejorCrom) contadorEstanc=0;
            else contadorEstanc++;
            if(verbosityLevel>=2) System.out.println("--------------RESUMEN ITERACIÓN--------------");
            if(verbosityLevel>=1)
                System.out.println("Iteración " + i + " | Mejor fitness: " + this.fitnessMejorSolucion +
                        " | Fitness prom. población: " + df1.format(this.fitnessSumadaPoblacion/this.tamPob) +
                        " | % mejora fitness población: " + df2.format(porcMejoraFitness*100) + "%" +
                        " | Nueva mejor solución? " + (cambioMejorCrom?"SI":"NO") +
                        " | Contador estancamiento: " + contadorEstanc + "/" + cantIterEstanc);
            if(verbosityLevel>=2)
                System.out.println("=".repeat(30));
            if(contadorEstanc==cantIterEstanc) break;
        }
        //Culminada la optimización, se calcula el tiempo de ejecución
        long tiempoFin = System.nanoTime();
        double tiempoMiliseg = (tiempoFin-tiempoIni)/(1000d*1000d);
        //Se guarda el tiempo de ejecución
        this.tiempoEjecucion = tiempoMiliseg;
        //Se ordena la población descendentemente por fitness
        Arrays.sort(poblacion,new ComparadorCromosomas());
        //Se calcula el fitness promedio de las mejores 10 y 20 soluciones
        double sumaMejoresFitness= 0;
        this.fitnessPromMejores10=0;
        this.fitnessPromMejores20=0;
        for(int i=0; i<Math.min(tamPob,20);i++){
            sumaMejoresFitness+= poblacion[i].getFitness();
            if(i==9) fitnessPromMejores10 = sumaMejoresFitness/10;
            if(i==19) fitnessPromMejores20 = sumaMejoresFitness/20;
        }
        double fitnessPeorCromosoma = poblacion[poblacion.length-1].getFitness();
        if(verbosityLevel>=0) {
            if (contadorEstanc == cantIterEstanc) System.out.println("Optimización terminada por estancamiento");
            else System.out.println("Optimización concluida exitosamente");
            System.out.println("=".repeat(15) + "RESULTADOS" + "=".repeat(15));
            System.out.println("Parámetros de ejecución:");
            System.out.println("numIter: " + this.numIter + "|tamPob: " + this.tamPob +
                    "|cantCromCruzados: " + this.cantCromCruzados + "|probMut: " + this.probMut +
                    "|cantHijosIngresados: " + this.cantHijosIngresados + "|cantIterEstanc: " + cantIterEstanc);
            System.out.println("Tiempo de ejecución: " + tiempoMiliseg + "ms");
            System.out.println("Fitness de la mejor solución de la población original:\t" + fitnessPrimerMejor);
            System.out.println("Fitness de la mejor solución de la población final:\t\t" + this.fitnessMejorSolucion);
            System.out.println("Fitness promedio de la población final:\t\t\t\t\t" + (this.fitnessSumadaPoblacion / this.tamPob));
            System.out.println("Fitness promedio de las 10 mejores soluciones finales:\t" + fitnessPromMejores10);
            System.out.println("Fitness promedio de las 20 mejores soluciones finales:\t" + fitnessPromMejores20);
            System.out.println("Fitness del peor cromosoma de la población final:\t\t" + fitnessPeorCromosoma);
            System.out.println("Mejor solución de la población inicial:");
            System.out.println(primerMejor);
            System.out.println("Mejor solución general:");
            System.out.println(this.mejorSolucion);
        }
        return this.mejorSolucion;
    }

    public void inicializarPoblacion(){
        this.fitnessMejorSolucion = -1;
        this.poblacion = new Cromosoma[tamPob];
        this.fitnessSumadaPoblacion = 0;
        for(int i=0; i<tamPob; i++){
            Cromosoma cromosomaNuevo = new Cromosoma();
            double fitnessCrom = cromosomaNuevo.getFitness();
            this.fitnessSumadaPoblacion += fitnessCrom;
            this.poblacion[i] = cromosomaNuevo;
            if(Double.compare(fitnessCrom,this.fitnessMejorSolucion)>0){
                this.fitnessMejorSolucion = fitnessCrom;
                this.mejorSolucion = cromosomaNuevo;
                this.indiceMejorSolucion = i;
            }
        }
    }

    public Cromosoma[] seleccionXTorneo(SplittableRandom genRandom){
        //Inicializar arreglo de padres
        Cromosoma[] arrPadres = new Cromosoma[this.cantCromCruzados];
        //Se aplica la selección por cada padre a elegir
        for(int i=0; i<this.cantCromCruzados; i++){
            //Se selecciona dos individuos de la población diferentes de manera aleatoria
            int indice1= genRandom.nextInt(this.tamPob);
            int indice2;
            do{
                indice2= genRandom.nextInt(this.tamPob);
            } while (indice1 == indice2);

            Cromosoma candidato1 = this.poblacion[indice1];
            Cromosoma candidato2 = this.poblacion[indice2];

            //Entra al arreglo de padres el que tiene la mayor fitness de los dos

            if(Double.compare(candidato1.getFitness(), candidato2.getFitness()) >=0){
                arrPadres[i]= candidato1;
            }else{
                arrPadres[i]= candidato2;
            }
        }

        return arrPadres;
    }

    public Cromosoma[] casamientoPMX(Cromosoma[] arrPadres, SplittableRandom genRandom){
        //Inicializar arreglo de hijos (se garantizó en el constructor que el número de padres sea par)
        Cromosoma[] arrHijos = new Cromosoma[arrPadres.length/2];
        //Tomar padres de 2 en dos y aplicarles casamiento PMX
        for(int i=0; i<arrPadres.length; i+=2){
            int[] arrPrimerPadre = arrPadres[i].getCromosoma();
            int[] indicesPrimerPadre = arrPadres[i].getIndicesTab();
            int[] arrSegundoPadre = arrPadres[i+1].getCromosoma();
            int[] indicesSegundoPadre = arrPadres[i+1].getIndicesTab();
            //Generar arreglo del cromosoma hijo. Primero se llena de ceros
            int numTablas = arrPrimerPadre.length;
            int[] arrHijo = new int[numTablas];
            arrHijo[0] = 0;
            for(int j=1;j<numTablas;j+=j){
                System.arraycopy(arrHijo,0,arrHijo, j, Math.min((numTablas - j), j));
            }
            //Se genera también su arreglo de índices por tabla
            int[] indicesHijo = new int[numTablas];
            //Escoger dos índices aleatoriamente
            int indiceIni = genRandom.nextInt(numTablas);
            int indiceFin = indiceIni + genRandom.nextInt(numTablas-indiceIni);
            //Copiar el segmento del primer padre limitado por los índices al arreglo del hijo
            System.arraycopy(arrPrimerPadre,indiceIni,arrHijo,indiceIni,(indiceFin-indiceIni+1));
            //Se revisa el segmento en cada padre para identificar elementos del segundo que no estén en el primero
            for(int j=indiceIni;j<=indiceFin;j++){
                //Aprovechamos para llenar los índices del segmento copiado al hijo
                int idTabPadre1 = arrPrimerPadre[j]/100;
                indicesHijo[idTabPadre1-1] = j;
                int idTabPadre2 = arrSegundoPadre[j]/100;
                if(indicesPrimerPadre[idTabPadre2-1]>=indiceIni && indicesPrimerPadre[idTabPadre2-1]<=indiceFin) continue;
                //Si no está en el segmento del padre 1, se encuentra una posición en la que colocarlo
                int indiceColocacion = j; //Índice del elemento que falta colocar
                do{
                    //Se determina què elemento del padre 1 está en dicho índice
                    idTabPadre1 = arrPrimerPadre[indiceColocacion]/100;
                    //Se busca el índice de ese elemento en el padre 2
                    indiceColocacion = indicesSegundoPadre[idTabPadre1-1];
                }while(arrHijo[indiceColocacion]!=0); //El proceso se repite hasta encontrar un lugar disp. en el hijo
                //El elemento faltante se coloca en el hijo
                arrHijo[indiceColocacion] = arrSegundoPadre[j];
                //Se actualiza el arreglo de índices
                indicesHijo[idTabPadre2-1] = indiceColocacion;
            }
            //En los espacios que quedan se copian los elementos del padre 2
            for(int j=0;j<numTablas;j++){
                if(arrHijo[j]!=0)continue;
                arrHijo[j] = arrSegundoPadre[j];
                //Copiamos la información de índices también
                int indiceTabla = arrHijo[j]/100-1;
                indicesHijo[indiceTabla] = indicesSegundoPadre[indiceTabla];
            }

            //Con esto listo, creamos el cromosoma y lo añadimos al arreglo de hijos
            Cromosoma nuevoHijo = new Cromosoma(arrHijo,indicesHijo);
            arrHijos[i/2] = nuevoHijo;
        }

        return arrHijos;
    }

    public Cromosoma[] inversionMutation(Cromosoma[] arrHijos, int verbosityLevel, SplittableRandom genRandom){
        //Inicializar arreglo de hijos mutados
        Cromosoma[] arrHijosMut = new Cromosoma[arrHijos.length];
        //Recorrer el arreglo de hijos y, en caso se cumpla la probMutacion, se realiza swap mutation
        int contadorMutados = 0;
        for (int i = 0; i< arrHijos.length; i++){
            double random = genRandom.nextDouble();
            if(random >= this.probMut){
                //No se realiza mutación, el cromosoma original va al arreglo nuevo
                arrHijosMut[i] = arrHijos[i];
                continue;
            }
            //Se realiza la mutación
            // 0 1 2 3 4 5 6 7
            //Empezamos por copiar el cromosoma y el arreglo de índices
            int numTablas = DatosEntrada.getInstance(null).getNumTablas();
            int[] arrCromMut = arrHijos[i].getCromosoma().clone();
            int[] indCromMut = arrHijos[i].getIndicesTab().clone();
            //Se seleccionan dos índices aleatoriamente (pero se valida que no sean iguales)
            int indiceIni = genRandom.nextInt(numTablas-1);
            int indiceFin;
            if(numTablas==2) indiceFin = 1;
            else{
                do{indiceFin = genRandom.nextInt(numTablas);}while(indiceIni>=indiceFin);
            }
            //Se calcula el número de intercambios necesarios
            int numIntercambios = (indiceFin-indiceIni+1)/2; //Cantidad de elementos en el segmento divididad entre 2
            //Se procede a invertir el orden de los elementos en el segmento definido por los índices
            for(int j= 0; j<(indiceFin-indiceIni+1)/2; j++){
                int indiceIntercambio1 = indiceIni+j;
                int indiceIntercambio2 = indiceFin-j;
                //Se intercambian los índices en el arreglo de índices
                indCromMut[arrCromMut[indiceIntercambio1]/100-1] = indiceIntercambio2;
                indCromMut[arrCromMut[indiceIntercambio2]/100-1] = indiceIntercambio1;
                //Se intercambian los valores
                int temporal;
                temporal = arrCromMut[indiceIntercambio1];
                arrCromMut[indiceIntercambio1] = arrCromMut[indiceIntercambio2];
                arrCromMut[indiceIntercambio2] = temporal;
            }
            //Con estos cambios hechos, creamos el nuevo cromosoma y lo añadimos al arreglo
            Cromosoma mutacion = new Cromosoma(arrCromMut,indCromMut);
            arrHijosMut[i] = mutacion;
            contadorMutados++;
        }

        if(verbosityLevel>=2) System.out.println(contadorMutados + " hijos mutados de un total de " + arrHijos.length);
        return arrHijosMut;
    }

    public boolean evolPoblacion(Cromosoma[] arrHijos, SplittableRandom genRandom){
        //Para la mutación se seleccionarán aleatoriamente un cromosoma hijo y un cromosoma de la población
        //Siempre y cuando el de la población no sea el actualmente mejor, el hijo lo sustituirá
        //Flag que determina si hay un nuevo mejor cromosoma en la población
        boolean hayNuevoMejor = false;
        for(int i=0; i<this.cantHijosIngresados;i++){
            //Se obtiene aleatoriamente un índice del arreglo de hijos
            int indiceHijo = genRandom.nextInt(arrHijos.length);
            Cromosoma hijoEntrante = arrHijos[indiceHijo];
            //Se obtiene aleatoriamente un índice de la población (validando que no sea el mejor de la misma)
            int indicePob;
            if(this.tamPob==2) indicePob = 1- this.indiceMejorSolucion;
            else{
                do{ indicePob = genRandom.nextInt(this.tamPob); }while(indicePob!=this.indiceMejorSolucion);
            }
            Cromosoma individuoSaliente = this.poblacion[indicePob];
            //Antes de efectuar el reemplazo, actualizamos la suma total de fitness de la población
            this.fitnessSumadaPoblacion-=individuoSaliente.getFitness();
            this.fitnessSumadaPoblacion+=hijoEntrante.getFitness();
            //Hecho esto, se efectúa el reemplazo
            this.poblacion[indicePob] = hijoEntrante;
            //Se evalúa si se ha encontrado un nuevo mejor cromosoma
            if(Double.compare(hijoEntrante.getFitness(),this.fitnessMejorSolucion)>0){
                this.mejorSolucion = hijoEntrante;
                this.fitnessMejorSolucion = hijoEntrante.getFitness();
                this.indiceMejorSolucion = indicePob;
                //Se levanta el flag de mejora
                hayNuevoMejor = true;
            }
        }
        return hayNuevoMejor;
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

    public int getCantCromCruzados() {
        return cantCromCruzados;
    }

    public float getProbMut() {
        return probMut;
    }

    public float getPorcHijosIngresados() {
        return porcHijosIngresados;
    }

    public int getCantHijosIngresados() {
        return cantHijosIngresados;
    }

    public float getPorcIterEstancamiento() {
        return porcIterEstancamiento;
    }

    public Cromosoma[] getPoblacion() {
        return poblacion;
    }

    public double getFitnessSumadaPoblacion() {
        return fitnessSumadaPoblacion;
    }

    public Cromosoma getMejorSolucion() {
        return mejorSolucion;
    }

    public double getFitnessMejorSolucion() {
        return fitnessMejorSolucion;
    }

    public int getIndiceMejorSolucion() {
        return indiceMejorSolucion;
    }

    public double getTiempoEjecucion() {
        return tiempoEjecucion;
    }

    public double getFitnessPromMejores10() {
        return fitnessPromMejores10;
    }

    public double getFitnessPromMejores20() {
        return fitnessPromMejores20;
    }
}
