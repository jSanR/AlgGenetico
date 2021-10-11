package estructuras_problema;

import datos_param.DatosEntrada;

import java.util.SplittableRandom;

public class Cromosoma {
    private final int[] cromosoma; //Estructura de cromosoma descrita en el documento de tesis. Este es el cromosoma como tal.
    //Atributos correspondientes a cálculos para crear el cromosoma, modificarlo y determinar su fitness
    //Estructura que contiene el índice de cada tabla en el arreglo cromosoma. Permite acelerar búsquedas al aplicar
    //operadores genéticos
    private final int[] indicesTab;
    private double[] costProc; //Costos de procesamiento del plan de ejecución representado por el cromosoma
    private Tabla[] listaJoins;
    private double costCom;
    private double fitness;
    private boolean fitnessCalculada;

    public Cromosoma() {
        //Lógica para la creación de un cromosoma
        DatosEntrada datosEntrada = DatosEntrada.getInstance(null);
        int numTablas = datosEntrada.getNumTablas();
        int numSitios = datosEntrada.getNumSitios();
        boolean[][] distTabSit = datosEntrada.getDistTabSit();
        SplittableRandom random = new SplittableRandom();
        cromosoma = new int[numTablas];
        indicesTab = new int[numTablas];
        //Llenar el cromosoma de 0s (extraído de: https://stackoverflow.com/a/25508988)
        cromosoma[0] = 0;
        for(int i=1;i<numTablas;i+=i){
            System.arraycopy(cromosoma,0,cromosoma, i, Math.min((numTablas - i), i));
        }
        for(int i=0; i<numTablas;i++){
            //Se crea la primera mitad del elemento
            int elemento = (i+1)*100;
            //Se elige un sitio aleatoriamente en el que la tabla exista
            boolean[] distSitios = distTabSit[i];
            int indiceSitio;
            do{ indiceSitio = random.nextInt(numSitios); }while(!distSitios[indiceSitio]);
            //Se llena la segunda mitad del elemento
            elemento+= (indiceSitio+1);
            //Se elige un índice del cromosoma en donde colocar el elemento
            int indiceCrom;
            do{ indiceCrom = random.nextInt(numTablas); }while(cromosoma[indiceCrom]!=0);
            //Se coloca el elemento en la posición del índice
            cromosoma[indiceCrom] = elemento;
            //Se guarda el índice en el que está ubicado el elemento
            indicesTab[i]=indiceCrom;
        }
        //Inicializar el arreglo de costos de procesamiento en null
        costProc = null;
        listaJoins = null;
        fitnessCalculada = false;
    }

    //Constructor usado por ejemplo para generar cromosomas hijos o mutaciones
    public Cromosoma(int[] cromosoma, int[] indicesTablas) {
        this.cromosoma = cromosoma;
        this.indicesTab = indicesTablas;
        this.costProc = null;
        this.listaJoins = null;
        this.fitnessCalculada = false;
    }

    public Cromosoma(int[] cromosoma, int[] indicesTablas, double[] costProc, Tabla[] joins) {
        this.cromosoma = cromosoma.clone();
        this.indicesTab = indicesTablas.clone();
        this.costProc = costProc.clone();
        this.listaJoins = joins.clone();
        this.fitnessCalculada = false;
    }

    public Cromosoma(int[] cromosoma, int[] indicesTablas, double[] costProc, Tabla[] joins, double fitness) {
        this.cromosoma = cromosoma.clone();
        this.indicesTab = indicesTablas.clone();
        this.costProc = costProc.clone();
        this.listaJoins = joins.clone();
        this.fitness = fitness;
        this.fitnessCalculada = true;
    }

    private void calcularFitness(){
        //Lógica para el cálculo del fitness del cromosoma con base en la función objetivo del trabajo de tesis
        //y los datos de entrada
        DatosEntrada datos = DatosEntrada.getInstance(null);
        double costoProc;
        double costoCom;
        double costoTotal;
        if(this.costProc==null && this.listaJoins==null) costoProc= calcularCostProc(datos);
        else{
            costoProc=0;
            assert this.costProc != null;
            for(double costoJoin: this.costProc) costoProc+= costoJoin;
        }
        if(this.listaJoins!=null) costoCom= calcularCostCom(datos);
        else{
            costoCom=-1;
            System.err.println("ERROR: No se pudo calcular el costo de comunicación del cromosoma (lista de Joins vacía)");
            System.exit(4);
        }
        costoTotal=(datos.getCoefProc()*Math.log(costoProc+1)+datos.getCoefCom()*Math.log(costoCom+1))/10;
        this.fitness=1/costoTotal;
        this.fitnessCalculada = true;
    }

    private double calcularCostProc(DatosEntrada datos){
        //Inicializar arreglos de costos de proc. y joins
        this.costProc = new double[datos.getNumTablas()-1];
        this.listaJoins = new Tabla[datos.getNumTablas()-1];
        //Debug
        //Obtener estructuras necesarias para el cálculo del costo
        Tabla[] tablas = datos.getTablas();
        //Se recorrerá el arreglo del cromosoma para determinar los joins realizados
        // y calcular los costos en base a estos
        Tabla tabla1 = tablas[(this.cromosoma[0]/100)-1]; //La primera tabla del primer join es la del índice 0
        double costAcum=0; //Suma de costos de procesamiento
        for(int i=1;i<datos.getNumTablas();i++){
            //Se obtiene la segunda tabla del join
            Tabla tabla2 = tablas[(this.cromosoma[i]/100)-1];
            //Se calcula el costo del join entre tabla 1 y tabla 2 (usando la fórmula del documento de tesis)
            costProc[i-1] = tabla1.getNumFilas()*tabla2.getNumFilas()*1d;
            //Se calculan las cardinalidades de todas las columnas que corresponderán al join de ambas tablas
            int[] arrCardinalidades = new int[datos.getNumTotalColumnas()];
            long productoCard = 1; //El producto de las cardinalidades de las columnas comunes a usarse para el costo
            long prodCardColsT1 = 1; //Producto de las cardinalidades de tabla1 para las columnas comunes
            long prodCardColsT2 = 1; //Producto de las cardinalidades de tabla2 para las columnas comunes
            int contadorColumnas = 0;
            for(int j=0;j< arrCardinalidades.length;j++){
                int cardT1 = tabla1.getCardColumnas()[j];
                int cardT2 = tabla2.getCardColumnas()[j];
                if(cardT1*cardT2!=0){ //Si ambas cardinalidades existen (columna común a ambas tablas)
                    productoCard*= Math.max(cardT1, cardT2); //Sigue la fórmula de costo de procesamiento
                    prodCardColsT1*= cardT1;
                    prodCardColsT2*= cardT2;
                    arrCardinalidades[j] = Math.min(cardT1, cardT2); //Sigue el cálculo de cardinalidad de un join
                    contadorColumnas++;
                }
                else{ //La columna pertenece como máximo a una tabla
                    arrCardinalidades[j] = Math.max(cardT1, cardT2); //La cardinalidad es 0 si la col. no existe
                    contadorColumnas+= (cardT1+cardT2!=0?1:0); //El contador aumenta si la col. existe en alguna tabla
                }
            }
            costProc[i-1]/= productoCard;
            costAcum+=costProc[i-1];
            //Se arma el resultado del join entre tabla 1 y 2 como un objeto Tabla
            //Calcular número de filas
            int cardTuplaT1 = (int) Math.min(prodCardColsT1, tabla1.getNumFilas());
            int cardTuplaT2 = (int) Math.min(prodCardColsT2, tabla2.getNumFilas());
            int numFilas;
            if(cardTuplaT1<cardTuplaT2) numFilas = tabla1.getNumFilas();
            else if(cardTuplaT1>cardTuplaT2) numFilas = tabla2.getNumFilas();
            else numFilas = tabla1.getNumFilas()*tabla2.getNumFilas()/cardTuplaT1;
            //Se corrigen las cardinalidades (pues algunas pueden ser mayores al número de filas)
            for(int j=0;j<arrCardinalidades.length;j++){
                if(arrCardinalidades[j]>numFilas) arrCardinalidades[j] = numFilas;
            }
            //Se atrapa un error
            if(numFilas==0){
                System.err.println("Se generó un join con número de filas igual a 0");
                System.exit(4);
            }
            //Se crea el join como objeto Tabla
            tabla1 = new Tabla(-1, numFilas, contadorColumnas,
                    numFilas*contadorColumnas*datos.getTamPromColumna(),true, arrCardinalidades);
            //Se guarda el join en la lista de joins
            this.listaJoins[i-1] = tabla1;
        }
        return costAcum;
    }

    private double calcularCostCom(DatosEntrada datos){
        //Obtener estructuras necesarias para el cálculo del costo
        Tabla[] tablas = datos.getTablas();
        int [][] capTransSitios = datos.getCapTransSitios();
        double costoAcum=0;
        //Se evalúa si cada join conlleva una transmisión
        Tabla tabla1 = tablas[(this.cromosoma[0]/100)-1]; //La primera tabla del primer join es la del índice 0
        for(int i=1;i< tablas.length;i++){
            //Se determinan los sitios de donde se obtienen las tablas 1 y 2
            int idSit1 = this.cromosoma[i-1]%100;
            int idSit2 = this.cromosoma[i]%100;
            //Si los sitios son diferentes, esto implica una transmisión entre sitios
            if(idSit1!=idSit2){
                //La transmisión implica un overhead + el tiempo de transmisión de la tabla 1 entre los sitios
                costoAcum+= datos.getOverheadTrans()/1000d + tabla1.getNumBytes()*1.0d/capTransSitios[idSit1-1][idSit2-1];
            }
            //Tabla1 pasa a ser el join generado entre 1 y 2
            tabla1 = this.listaJoins[i-1];
        }
        this.costCom = costoAcum;
        return costoAcum;
    }

    public Cromosoma clonar(){
        //TODO verificar qué tan óptimo es usar este método para clonar arreglos
        int[] arrCromNuevo = this.cromosoma.clone();
        int[] indCromNuevo = this.indicesTab.clone();
        return new Cromosoma(arrCromNuevo,indCromNuevo);
    }

    public int[] getCromosoma() {
        return cromosoma;
    }

    public int[] getIndicesTab() {
        return indicesTab;
    }

    public double[] getCostProc() {
        return costProc;
    }

    public Tabla[] getListaJoins() {
        return listaJoins;
    }

    public double getFitness() {
        if(!this.fitnessCalculada){
            calcularFitness();
        }
        return fitness;
    }

    public boolean isFitnessCalculada() {
        return fitnessCalculada;
    }

    public void setFitnessCalculada(boolean fitnessCalculada) {
        this.fitnessCalculada = fitnessCalculada;
    }

    @Override
    public String toString() {
        StringBuilder salida = new StringBuilder("Cromosoma{");
        for(int i = 0; i<cromosoma.length; i++){
            int elemento = cromosoma[i];
            salida.append("T").append(elemento / 100).append("(S").append(elemento % 100).append(")");
            if(i!= cromosoma.length-1) salida.append("=>");
        }
        salida.append("}, fitness: ").append(fitnessCalculada ? fitness : "No calculada");
        salida.append(", Costos de proc: {");
        for(int i=0; i<cromosoma.length-1;i++){
            salida.append(" ").append(this.costProc[i]);
        }
        salida.append(" }, Costo de com: ").append(this.costCom);
        return salida.toString();
    }

    public double getCostCom() {
        return costCom;
    }


}
