package estructuras_problema;

import datos_param.DatosEntrada;
import org.junit.jupiter.api.*;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class CromosomaTest {

    private static DatosEntrada datos;

    @BeforeAll
    static void obtenerDatos(){
        datos = DatosEntrada.getInstance("D:\\Libraries\\Documentos\\PUCP\\C10\\TESIS2\\datos.csv");
    }

    @AfterAll
    static void liberarDatos(){
        DatosEntrada.discardInstance();
    }

    @RepeatedTest(value = 10)
    @DisplayName("Comprueba 10 veces que un cromosoma se forme válidamente mediante su constructor vacío")
    void shouldCreateCorrectly(){
        Cromosoma cromEvaluado = new Cromosoma();
        int[] indicesTabla = cromEvaluado.getIndicesTab();
        int[] arrCrom = cromEvaluado.getCromosoma();
        int numTablas = datos.getNumTablas();
        int numSitios = datos.getNumSitios();
        boolean[][] distTabSit = datos.getDistTabSit();
        assertNotNull(arrCrom);
        assertNotNull(indicesTabla);
        assertNull(cromEvaluado.getCostProc());
        assertNull(cromEvaluado.getListaJoins());
        assertFalse(cromEvaluado.isFitnessCalculada());

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

    @Test
    @DisplayName("Comprueba que el fitness se haya calculado correctamente")
    void shouldCalcularteFitness(){
        Cromosoma cromEvaluado = new Cromosoma();
        assertFalse(cromEvaluado.isFitnessCalculada());
        assertNull(cromEvaluado.getCostProc());
        assertNull(cromEvaluado.getListaJoins());
        double fitnessCalc = cromEvaluado.getFitness();
        assertNotNull(cromEvaluado.getCostProc());
        assertNotNull(cromEvaluado.getListaJoins());
        assertEquals(cromEvaluado.getCostProc().length, datos.getNumTablas()-1);
        assertEquals(cromEvaluado.getListaJoins().length, datos.getNumTablas()-1);
        assertTrue(cromEvaluado.isFitnessCalculada());
        assertTrue(fitnessCalc>0);
        //double fitnessDatos = cromEvaluado.getCostCom() + Arrays.stream(cromEvaluado.getCostProc()).sum();
    }
}