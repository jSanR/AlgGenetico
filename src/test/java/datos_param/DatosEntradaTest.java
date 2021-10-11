package datos_param;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class DatosEntradaTest {

    @AfterEach
    void discardInstance(){
        DatosEntrada.discardInstance();
    }

    @Test
    @DisplayName("Comprueba que los datos de entrada sean correctos si se usa una ruta relativa")
    void shouldGetInstanceNoPath() {
        DatosEntrada datos= DatosEntrada.getInstance(null);
        assertNotNull(datos);
        assertTrue(datos.getNumTablas()>1);
        assertNotNull(datos.getTablas());
        assertNotNull(datos.getCardTablas());
        assertNotNull(datos.getCapTransSitios());
        assertNotNull(datos.getDistTabSit());
        assertEquals(datos.getNumTablas(),datos.getTablas().length);
        assertEquals(datos.getNumTablas(),datos.getCardTablas().length);
        assertEquals(datos.getNumTablas(),datos.getDistTabSit().length);
        assertTrue(datos.getNumSitios()>0);
        assertEquals(datos.getNumSitios(),datos.getDistTabSit()[0].length);
        assertEquals(datos.getNumSitios(),datos.getCapTransSitios().length);
        assertEquals(datos.getNumSitios(),datos.getCapTransSitios()[0].length);
        assertTrue(datos.getNumTotalColumnas()>1);
        assertEquals(datos.getNumTotalColumnas(),datos.getCardTablas()[0].length);
    }

    @Test
    @DisplayName("Comprueba que los datos de entrada sean correctos si se especifica un path")
    @DisabledOnOs(value = OS.LINUX, disabledReason = "Path invalid on Linux")
    void shouldGetInstancePath() {
        DatosEntrada datos= DatosEntrada.getInstance("D:\\Libraries\\Documentos\\PUCP\\C10\\TESIS2\\datos.csv");
        assertNotNull(datos);
        assertTrue(datos.getNumTablas()>1);
        assertNotNull(datos.getTablas());
        assertNotNull(datos.getCardTablas());
        assertNotNull(datos.getCapTransSitios());
        assertNotNull(datos.getDistTabSit());
        assertEquals(datos.getNumTablas(),datos.getTablas().length);
        assertEquals(datos.getNumTablas(),datos.getCardTablas().length);
        assertEquals(datos.getNumTablas(),datos.getDistTabSit().length);
        assertTrue(datos.getNumSitios()>0);
        assertEquals(datos.getNumSitios(),datos.getDistTabSit()[0].length);
        assertEquals(datos.getNumSitios(),datos.getCapTransSitios().length);
        assertEquals(datos.getNumSitios(),datos.getCapTransSitios()[0].length);
        assertTrue(datos.getNumTotalColumnas()>1);
        assertEquals(datos.getNumTotalColumnas(),datos.getCardTablas()[0].length);
    }

    @ParameterizedTest
    @DisplayName("Comprueba que el programa finalice si se pasa un archivo incorrecto o inexistente como path")
    @DisabledOnOs(value = OS.LINUX, disabledReason = "Paths invalid on Linux")
    @ValueSource(strings = {"D:\\Libraries\\Documentos\\PUCP\\C10\\TESIS2\\datos_incorrectos.csv", "D:\\archivo_inexistente.csv"})
    @ExpectSystemExitWithStatus(value = 2)
    void shouldNotGetInstance(String path) {
        DatosEntrada datos = DatosEntrada.getInstance(path);
    }
}