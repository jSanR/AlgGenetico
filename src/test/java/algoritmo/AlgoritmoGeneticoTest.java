package algoritmo;

import datos_param.DatosEntrada;
import estructuras_problema.Cromosoma;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class AlgoritmoGeneticoTest {

    private static DatosEntrada datos;

    @BeforeAll
    static void obtenerDatos(){
        datos = DatosEntrada.getInstance("D:\\Libraries\\Documentos\\PUCP\\C10\\TESIS2\\datos_prueba_algoritmo.csv");
    }

    @AfterAll
    static void liberarDatos(){
        DatosEntrada.discardInstance();
    }

    @Test
    @Order(value = 1)
    @DisplayName("Comprueba que la población se inicie correctamente")
    void inicializarPoblacion(){
        AlgoritmoGenetico algEval = new AlgoritmoGenetico();
        assertNull(algEval.getPoblacion());
        algEval.inicializarPoblacion();
        assertNotNull(algEval.getPoblacion());
        assertNotNull(algEval.getMejorSolucion());
        assertTrue(algEval.getFitnessMejorSolucion()>0);
        assertTrue(algEval.getFitnessSumadaPoblacion()>0);
        assertTrue(algEval.getIndiceMejorSolucion()>=0 && algEval.getIndiceMejorSolucion()<algEval.getTamPob());
        assertEquals(algEval.getTamPob(), algEval.getPoblacion().length);
        //Comprobar validez de cromosomas
        comprobarValidezArrCromosomas(algEval.getPoblacion());
    }

    @Test
    @Order(value = 2)
    @DisplayName("Comprueba que la la selección se realiza correctamente")
    void seleccionXRuleta() {
        AlgoritmoGenetico algEval = new AlgoritmoGenetico();
        algEval.inicializarPoblacion();
        SplittableRandom genRandom = new SplittableRandom();
        Cromosoma[] padres = algEval.seleccionXTorneo(genRandom);
        assertEquals(algEval.getCantCromCruzados(), padres.length);
        //Comprobar validez de cromosomas
        comprobarValidezArrCromosomas(padres);
    }

    @Test
    @Order(value = 3)
    @DisplayName("Comprueba que el casamiento genera hijos válidos")
    void casamientoPMX() {
        AlgoritmoGenetico algEval = new AlgoritmoGenetico();
        algEval.inicializarPoblacion();
        SplittableRandom genRandom = new SplittableRandom();
        Cromosoma[] padres = algEval.seleccionXTorneo(genRandom);
        Cromosoma[] hijos = algEval.casamientoPMX(padres, genRandom);
        assertEquals(algEval.getCantCromCruzados()/2, hijos.length);
        //Comprobar validez de hijos
        comprobarValidezArrCromosomas(hijos);
    }

    @RepeatedTest(value = 5)
    @Order(value = 4)
    @DisplayName("Comprueba que el la mutación genera cromosomas válidos")
    void swapMutation() {
        AlgoritmoGenetico algEval = new AlgoritmoGenetico();
        algEval.inicializarPoblacion();
        SplittableRandom genRandom = new SplittableRandom();
        Cromosoma[] padres = algEval.seleccionXTorneo(genRandom);
        Cromosoma[] hijos = algEval.casamientoPMX(padres, genRandom);
        Cromosoma[] hijosMut = algEval.inversionMutation(hijos,2, genRandom);
        assertEquals(algEval.getCantCromCruzados()/2, hijos.length);
        assertEquals(hijos.length, hijosMut.length);
        //Comprobar validez de hijos mutados
        comprobarValidezArrCromosomas(hijosMut);
    }

    @Test
    @Order(value = 6)
    @DisplayName("Comprueba que la población evoluciona con éxito")
    void evolPoblacion() {
        AlgoritmoGenetico algEval = new AlgoritmoGenetico();
        algEval.inicializarPoblacion();
        SplittableRandom genRandom = new SplittableRandom();
        Cromosoma[] padres = algEval.seleccionXTorneo(genRandom);
        Cromosoma[] hijos = algEval.casamientoPMX(padres, genRandom);
        Cromosoma[] hijosMut = algEval.inversionMutation(hijos,2, genRandom);
        assertEquals(algEval.getCantCromCruzados()/2, hijosMut.length);
        //Calcular fitness de hijosBusq
        for(int i=0; i< hijosMut.length; i++) hijosMut[i].getFitness();
        //Revisar fitness del mejor cromosoma actual
        double fitnessMejorInicial = algEval.getFitnessMejorSolucion();
        boolean hayNuevoMejor = algEval.evolPoblacion(hijosMut, genRandom);
        if(hayNuevoMejor){
            double fitnessNuevoMejor = algEval.getFitnessMejorSolucion();
            assertTrue(fitnessNuevoMejor > fitnessMejorInicial);
        }
        //Comprobar validez de la población
        comprobarValidezArrCromosomas(algEval.getPoblacion());
    }

    void comprobarValidezArrCromosomas(Cromosoma[] arrCrom){
        for (Cromosoma cromEval: arrCrom) comprobarValidezCromosoma(cromEval);
    }

    void comprobarValidezCromosoma(Cromosoma cromEvaluado){
        assertNotNull(cromEvaluado);
        int[] indicesTabla = cromEvaluado.getIndicesTab();
        int[] arrCrom = cromEvaluado.getCromosoma();
        int numTablas = datos.getNumTablas();
        int numSitios = datos.getNumSitios();
        boolean[][] distTabSit = datos.getDistTabSit();
        assertNotNull(arrCrom);
        assertNotNull(indicesTabla);

        assertEquals(arrCrom.length, numTablas);
        assertEquals(indicesTabla.length, numTablas);

        int[] indOrdenados = Arrays.stream(indicesTabla).sorted().toArray();
        int[] cromOrdenado = Arrays.stream(arrCrom).sorted().toArray();
        for(int i=0; i< numTablas; i++){
            assertEquals(i, indOrdenados[i]);
            assertTrue(cromOrdenado[i]/100 >0 && cromOrdenado[i]/100 <= numTablas);
            assertTrue(cromOrdenado[i]%100 >0 && cromOrdenado[i]%100 <= numSitios);
            assertEquals(i+1, cromOrdenado[i]/100);
            assertTrue(distTabSit[cromOrdenado[i]/100-1][cromOrdenado[i]%100-1]);
        }
    }
}