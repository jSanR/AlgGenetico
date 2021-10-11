package datos_param;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import estructuras_problema.Tabla;

public class DatosEntrada {
    //Esta clase contiene todos los datos de entrada del problema.
    private final int numTablas; //NumTab
    private final int numSitios; //NumSit
    private final int numTotalColumnas;
    private final boolean[][] distTabSit; //DistTabSit
    private final Tabla[] tablas; //datos específicos a cada tabla
    private final int [][] cardTablas; //CardTab
    private final int [][] capTransSitios; //CapTrans
    private final int tamPromColumna; //Tamaño promedio de una columna de base de datos en bytes
    private final int overheadTrans; //Overhead al realizar una transmisión cualquiera entre dos sitios (en milisegundos)
    private final float coefCom;
    private final float coefProc;
    private final ParametrosAlgoritmo[] paramAlg;
    private final int verbosityLevel;

    //Necesario para aplicar el patrón Singleton en esta clase
    private static DatosEntrada INSTANCIA;

    public DatosEntrada(int numTablas, int numSitios, int numTotalColumnas, boolean[][] distTabSit, Tabla[] tablas, int[][] cardTablas, int[][] capTransSitios, int tamPromColumna, int overheadTrans, float coefCom, float coefProc, ParametrosAlgoritmo[] paramAlg, int verbosityLevel) {
        this.numTablas = numTablas;
        this.numSitios = numSitios;
        this.numTotalColumnas = numTotalColumnas;
        this.distTabSit = distTabSit;
        this.tablas = tablas;
        this.cardTablas = cardTablas;
        this.capTransSitios = capTransSitios;
        this.tamPromColumna = tamPromColumna;
        this.overheadTrans = overheadTrans;
        this.coefCom = coefCom;
        this.coefProc = coefProc;
        this.paramAlg = paramAlg;
        this.verbosityLevel = verbosityLevel;
    }

    public static DatosEntrada getInstance(String pathArchivoDatos){
        if(INSTANCIA == null){
            INSTANCIA = loadInstance(pathArchivoDatos);
        }
        return INSTANCIA;
    }

    private static DatosEntrada loadInstance(String pathArchivoDatos){
        //Lógica para leer el archivo de datos de entrada
        String path;
        String separadorCsv = ";";
        //Obtener path del archivo de
        if(pathArchivoDatos == null)
            path = (new File(DatosEntrada.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent()) + "/datos.csv";
        else path = pathArchivoDatos;
        File archivoDatos = new File(path);
        //Verificar que el archivo exista
        try{
            FileReader fileReader= new FileReader(archivoDatos);
            BufferedReader csvReader = new BufferedReader(fileReader);
            //Lectura del archivo de datos
            int numTablas;
            int numSitios;
            int numTotalColumnas;
            boolean[][] distTabSit;
            Tabla[] tablas;
            int[][] cardTablas;
            int[][] capTrans;
            int tamPromColumna;
            int overheadTrans;
            float coefCom;
            float coefProc;
            ParametrosAlgoritmo[] combParamAlg;
            //Primera línea, datos del problema (numTab, numSit, numTotalCol, tamPromCol, overheadTransm, coefCom)
            String fila = csvReader.readLine();
            if(fila==null){
                throw new IOException("ERROR: El archivo de datos está vacío");
            }
            String[] datos = fila.split(separadorCsv);
            if(datos.length!=6){
                throw new IOException("ERROR: Faltan datos del problema en el archivo de datos (linea 1)");
            }
            //Se llenan los datos
            numTablas=Integer.parseInt(datos[0]);
            numSitios=Integer.parseInt(datos[1]);
            numTotalColumnas=Integer.parseInt(datos[2]);
            tamPromColumna=Integer.parseInt(datos[3]);
            overheadTrans=Integer.parseInt(datos[4]);
            coefCom=Float.parseFloat(datos[5]);
            if(numTablas <= 1 || numSitios <=0 || numTotalColumnas <=0 || tamPromColumna <= 0 || overheadTrans < 0 || coefCom <0){
                throw new IOException("ERROR: Datos inválidos en la línea 1 del archivo de datos");
            }
            coefProc=1-coefCom;
            //Segunda línea, se debe leer un guion que separa la siguiente sección de datos
            fila = csvReader.readLine();
            if(fila==null){
                throw new IOException("ERROR: El archivo de datos está incompleto (solamente se leyó primera línea");
            }
            //Sección de información de tablas
            tablas = new Tabla[numTablas];
            cardTablas = new int[numTablas][numTotalColumnas];
            for(int i=0;i<numTablas;i++){
                fila = csvReader.readLine();
                if(fila==null){
                    throw new IOException("ERROR: El archivo de datos está incompleto (solamente se leyó hasta la línea " + (2+i));
                }
                datos = fila.split(separadorCsv);
                if(datos.length!=numTotalColumnas+2 || datos[0].equals("-")){
                    throw new IOException("ERROR: Archivo de datos - Faltan datos para la tabla " + (i+1) + " (linea " + (3+i) + ")");
                }
                //Datos de "cabecera" de la tabla
                int numFilas = Integer.parseInt(datos[0]);
                int numBytes = Integer.parseInt(datos[1]);
                if(numFilas <=0 || numBytes <=0){
                    throw new IOException("ERROR: Archivo de datos - Datos de cabecera incorrectos para la tabla " + (i+1) + " (linea " + (3+i) + ")");
                }
                //Lectura de cardinalidades de la tabla
                int[] cardColumnas = cardTablas[i];
                int numColumnas = 0;
                for (int j=0;j<numTotalColumnas;j++){
                    cardColumnas[j] = Integer.parseInt(datos[j+2]);
                    if(cardColumnas[j]<0){
                        throw new IOException("ERROR: Archivo de datos - Cardinalidad negativa para la tabla " + (i+1) + ", columna " + (j+1) + " (linea " + (3+i) + ")");
                    }
                    if(cardColumnas[j]>numFilas){
                        throw new IOException("ERROR: Archivo de datos - Cardinalidad mayor a numFilas para la tabla " + (i+1) + ", columna " + (j+1) + " (linea " + (3+i) + ")");
                    }
                    if(cardColumnas[j]!=0)numColumnas++;
                }
                Tabla nuevaTabla = new Tabla(i+1,numFilas,numColumnas,numBytes,cardColumnas);
                tablas[i] = nuevaTabla;
            }
            //Segundo guión separador
            fila = csvReader.readLine();
            if(fila==null){
                throw new IOException("ERROR: El archivo de datos está incompleto (líneas leídas: " + (numTablas+2) + ")");
            }
            //Sección de distribución de tablas en sitios
            distTabSit = new boolean[numTablas][numSitios];
            for(int i=0;i<numTablas;i++){
                fila = csvReader.readLine();
                if(fila==null){
                    throw new IOException("ERROR: El archivo de datos está incompleto (solamente se leyó hasta la línea " + (numTablas+3+i) + ")");
                }
                datos = fila.split(separadorCsv);
                if(datos.length!=numSitios || datos[0].equals("-")){
                    throw new IOException("ERROR: Archivo de datos - Faltan datos de dist. para la tabla " + i+1 + " (linea " + (numTablas+4+i) + ")");
                }
                int numSitiosDisp=0;
                boolean[] distTabla = distTabSit[i];
                for(int j=0; j<numSitios;j++){
                    int valor = Integer.parseInt(datos[j]);
                    if(valor==0) distTabla[j] = false;
                    else{
                        distTabla[j] = true;
                        numSitiosDisp++;
                    }
                }
                //La tabla debe existir en al menos un sitio
                if(numSitiosDisp==0){
                    throw new IOException("ERROR: Archivo de datos - La tabla " + i+1 + " no existe en ningún sitio (linea " + (numTablas+4+i) + ")");
                }
            }
            //Tercer guion separador
            fila = csvReader.readLine();
            if(fila==null){
                throw new IOException("ERROR: El archivo de datos está incompleto (líneas leídas: " + (numTablas*2+3) + ")");
            }
            //Sección de capacidades de transmisión entre tablas
            capTrans = new int[numSitios][numSitios];
            for(int i=0;i<numSitios;i++){
                fila = csvReader.readLine();
                if(fila==null){
                    throw new IOException("ERROR: El archivo de datos está incompleto (solamente se leyó hasta la línea " + (numTablas*2+4+i) + ")");
                }
                datos = fila.split(separadorCsv);
                if(datos.length!=numSitios || datos[0].equals("-")){
                    throw new IOException("ERROR: Archivo de datos - Faltan datos de cap. de trans. para el sitio " + i+1 + " (linea " + (numTablas*2+5+i) + ")");
                }
                for(int j=i; j<numSitios;j++){
                    //En la diagonal de la matriz se fuerzan capacidades de trans. de 0
                    if(i==j) capTrans[i][j] = 0;
                    else{
                        int capacidad = Integer.parseInt(datos[j]);
                        //Capacidad inválida
                        if(capacidad<=0){
                            throw new IOException("ERROR: Archivo de datos - Cap. de trans. inválida para los sitios " + i+1 + " y " + j+1 + " (linea " + (numTablas*2+5+i) + ")");
                        }
                        capTrans[i][j] = capacidad;
                        capTrans[j][i] = capacidad;
                    }
                }
            }
            //Cuarto y último guion separador
            //Tercer guion separador
            fila = csvReader.readLine();
            if(fila==null){
                throw new IOException("ERROR: El archivo de datos está incompleto (líneas leídas: " + (numTablas*2+numSitios+4) + ")");
            }
            //Sección de parámetros de ejecución del algoritmo
            ArrayList<ParametrosAlgoritmo> combinacionesParam = new ArrayList<>();
            int contadorCombinaciones = 0;
            int verbosityLevel=-1;
            while(true) {
                fila = csvReader.readLine();
                if (fila == null) {
                    if(contadorCombinaciones==0)
                        throw new IOException("ERROR: El archivo de datos está incompleto (líneas leídas: " + (numTablas * 2 + numSitios + 5 + contadorCombinaciones) + ")");
                    else break;
                }
                datos = fila.split(separadorCsv);
                if (datos.length != 9 || datos[0].equals("-")) {
                    throw new IOException("ERROR: Archivo de datos - Número incorrecto de parámetros de ejecución del algoritmo (linea " + (numTablas * 2 + numSitios + 6 + contadorCombinaciones) + ")");
                }
                int numIter = Integer.parseInt(datos[0]);
                int tamPob = Integer.parseInt(datos[1]);
                float porcCruce = Float.parseFloat(datos[2]);
                float probMut = Float.parseFloat(datos[3]);
                float probBusq = Float.parseFloat(datos[4]);
                float porcHijos = Float.parseFloat(datos[5]);
                int cantVecinosEval = Integer.parseInt(datos[6]);
                float porcIterEstanc = Float.parseFloat(datos[7]);
                if(contadorCombinaciones==0) verbosityLevel = Integer.parseInt(datos[8]);
                if (numIter <= 0 || tamPob <= 0 || porcCruce > 1 || probMut > 1 || probBusq > 1
                        || porcHijos > 1 || cantVecinosEval <= 0 || porcIterEstanc > 1) {
                    throw new IOException("ERROR: Datos inválidos en la sección de parámetros de ejecución del algoritmo");
                }
                ParametrosAlgoritmo paramAlg = new ParametrosAlgoritmo(numIter, tamPob, porcCruce, probMut, probBusq, porcHijos, cantVecinosEval, porcIterEstanc);
                combinacionesParam.add(paramAlg);
                contadorCombinaciones++;
            }
            combParamAlg = new ParametrosAlgoritmo[combinacionesParam.size()];
            int indiceParam = 0;
            for(ParametrosAlgoritmo param: combinacionesParam){
                combParamAlg[indiceParam] = param;
                indiceParam++;
            }
            DatosEntrada datosEntrada =
                    new DatosEntrada(numTablas,numSitios,numTotalColumnas,distTabSit,tablas,cardTablas,
                            capTrans,tamPromColumna,overheadTrans,coefCom,coefProc,combParamAlg, verbosityLevel);
            return datosEntrada;
        }catch (NumberFormatException | IOException ex){
            System.err.println(ex.getMessage());
            ex.printStackTrace();
            System.exit(2);
        }
        return null;
    }

    public int getNumTablas() {
        return numTablas;
    }

    public int getNumSitios() {
        return numSitios;
    }

    public int getNumTotalColumnas() {
        return numTotalColumnas;
    }

    public boolean[][] getDistTabSit() {
        return distTabSit;
    }

    public Tabla[] getTablas() {
        return tablas;
    }

    public int[][] getCardTablas() {
        return cardTablas;
    }

    public int[][] getCapTransSitios() {
        return capTransSitios;
    }

    public int getTamPromColumna() {
        return tamPromColumna;
    }

    public int getOverheadTrans() {
        return overheadTrans;
    }

    public float getCoefCom() {
        return coefCom;
    }

    public float getCoefProc() {
        return coefProc;
    }

    public ParametrosAlgoritmo[] getParamAlg() {
        return paramAlg;
    }

    public int getVerbosityLevel() {
        return verbosityLevel;
    }

    @Override
    public String toString() {
        return "datos_param.DatosEntrada{" +
                "numTablas=" + numTablas +
                ", numSitios=" + numSitios +
                ", numTotalColumnas=" + numTotalColumnas +
                ", distTabSit=" + Arrays.toString(distTabSit) +
                ", cardTablas=" + Arrays.toString(cardTablas) +
                ", capTransSitios=" + Arrays.toString(capTransSitios) +
                ", tamPromColumna=" + tamPromColumna +
                ", overheadTrans=" + overheadTrans +
                ", coefCom=" + coefCom +
                ", coefProc=" + coefProc +
                ", paramAlg=" + paramAlg[0] +
                ", verbosityLevel=" + verbosityLevel +
                '}';
    }

    public static void discardInstance(){
        INSTANCIA=null;
    }
}
