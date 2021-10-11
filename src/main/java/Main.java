import algoritmo.AlgoritmoGenetico;
import datos_param.DatosEntrada;
import datos_param.ParametrosAlgoritmo;
import logging.MultiOutputStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args){
        //Valores de status de salida del programa:
        //0: Ejecución exitosa
        //1: Salida por error en la configuración de salida y error estándar
        //2: Salida por error en la lectura del archivo de datos de entrada
        //3: Salida por configuración inválida de parámetros de ejecución
        //4: Salida por error en el cálculo del fitness de un cromosoma
        //Configuración para que la salida y error estándar se copien a un archivo
        try {
            configurarSalida();
        }catch (FileNotFoundException ex){
            System.err.println(ex.getMessage());
            System.exit(1);
        }
        //Path en donde se ubica el archivo de datos. Se recomienda encarecidamente ingresar el path como argumento
        //del programa. Sin embargo, en caso de no hacerse, el programa intentará leer un archivo llamado "datos.csv"
        //que se ubique en el mismo directorio que el .jar
        String pathArchivoDatos;
        if(args.length == 1) pathArchivoDatos = args[0];
        else pathArchivoDatos = null;
        //Lectura de datos de entrada desde el archivo en cuestión
        DatosEntrada datos = DatosEntrada.getInstance(pathArchivoDatos);

        ejecutarAlgoritmo(datos);
        //ejecutarAlgCalibracion(datos,20);
        //imprimirCombParam();
    }

    public static void configurarSalida() throws FileNotFoundException{
        ZonedDateTime horaEjecucion = ZonedDateTime.now();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        //Obtenido de: https://www.codeproject.com/Tips/315892/A-quick-and-easy-way-to-direct-Java-System-out-to
        FileOutputStream fout = new FileOutputStream("out-" + horaEjecucion.format(formato) + ".txt");
        FileOutputStream ferr = new FileOutputStream("err-" + horaEjecucion.format(formato) + ".txt");

        MultiOutputStream multiOut = new MultiOutputStream(System.out, fout);
        MultiOutputStream multiErr = new MultiOutputStream(System.err, ferr);

        PrintStream stdout = new PrintStream(multiOut);
        PrintStream stderr = new PrintStream(multiErr);

        System.setOut(stdout);
        System.setErr(stderr);
    }

    public static void ejecutarAlgoritmo(DatosEntrada datos){
        //Ejecución única del algoritmo
        AlgoritmoGenetico algMem = new AlgoritmoGenetico();
        algMem.ejecutar(datos.getVerbosityLevel());
    }

    public static void ejecutarAlgCalibracion(DatosEntrada datos, int cantRepeticiones){
        //Ejecución múltiple del algoritmo bajo diferentes configuraciones de parámetros
        ParametrosAlgoritmo[] arrParametros = datos.getParamAlg();
        int i=1;
        for(ParametrosAlgoritmo config: arrParametros){
            //Se imprime la config de parámetros
            //System.out.println("Configuracion "+i+": Numiter="+config.getNumIter()+"|TamPob="+config.getTamPob()+
            //        "|PorcCromCruzados="+config.getPorcCromCruzados()+"|ProbMut="+config.getProbMut()+
            //        "|ProbBusq="+config.getProbBusq()+"|PorcHijosIngresados="+config.getPorcHijosIngresados()+
            //        "|CantVecinosEval="+config.getCantVecinosEvaluados()+"|PorcIterEstanc="+config.getPorcIterEstanc());
            StringBuilder salida = new StringBuilder();
            salida.append(config.getNumIter()).append(";").append(config.getTamPob()).append(";")
                    .append(config.getPorcCromCruzados()).append(";").append(config.getProbMut())
                    .append(";").append(config.getPorcHijosIngresados()).append(";")
                    .append(config.getPorcIterEstanc()).append(";|;");
            AlgoritmoGenetico algMem = new AlgoritmoGenetico(config.getNumIter(), config.getTamPob(), config.getPorcCromCruzados(),
                    config.getProbMut(), config.getPorcHijosIngresados(), config.getPorcIterEstanc());
            //Sumas de fitness y tiempos de ejecución
            double sumaFitnessMejorSol= 0;
            double sumaFitnessMejores10=0;
            double sumaFitnessMejores20=0;
            double sumaTiempoEjec=0;
            for(int j=0;j<cantRepeticiones;j++){
                algMem.ejecutar(-1);
                sumaFitnessMejorSol+= algMem.getFitnessMejorSolucion();
                sumaFitnessMejores10+= algMem.getFitnessPromMejores10();
                sumaFitnessMejores20+= algMem.getFitnessPromMejores20();
                sumaTiempoEjec+= algMem.getTiempoEjecucion();
            }
            //System.out.println((sumaFitnessMejorSol/cantRepeticiones)+";"+(sumaFitnessMejores10/cantRepeticiones)+";"+
            //        (sumaFitnessMejores20/cantRepeticiones)+";"+(sumaTiempoEjec/cantRepeticiones));
            salida.append(sumaFitnessMejorSol / cantRepeticiones).append(";")
                    .append(sumaFitnessMejores10 / cantRepeticiones).append(";")
                    .append(sumaFitnessMejores20 / cantRepeticiones).append(";")
                    .append(sumaTiempoEjec / cantRepeticiones);
            System.out.println(salida);
            i++;
        }
        System.out.println("-".repeat(20)+"FIN"+"-".repeat(20));
    }

    public static void imprimirCombParam(){
        int[] numIter = {5000,10000};
        int[] tamPob = {100,200};
        float[] porcCromCruzados = {0.5f,0.75f};
        float[] probMut = {0.5f,0.75f,1.0f};
        float[] probBusq = {0f};
        float[] porcHijosIngresados = {0.5f,0.75f,0.9f};
        int[] cantVecinos = {1};
        float porcIterEstanc = 0.3f;

        for(int iter: numIter){
            for(int tamano: tamPob){
                for(float porcCruce: porcCromCruzados){
                    for (float mut: probMut){
                        for(float busq: probBusq){
                            for(float porcSust: porcHijosIngresados){
                                for(int numVec: cantVecinos){
                                    System.out.println(iter+";"+tamano+";"+porcCruce+";"+
                                            mut+";"+busq+";"+porcSust+";"+numVec+";"+porcIterEstanc+";1");
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}
