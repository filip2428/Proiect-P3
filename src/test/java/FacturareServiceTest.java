import org.junit.jupiter.api.Test;
import org.example.model.Factura;
import org.example.model.Firma;
import org.example.service.FacturareService;
import static org.junit.jupiter.api.Assertions.*;

class FacturareServiceTest {

    @Test
    void testCalculRestDePlata() {
        Firma f = new Firma("Test SRL", "RO123", "Arad");
        // face o factura de 1000 de lei
        Factura fact = new Factura(1, 1000.0, f);
        FacturareService service = new FacturareService();

        // plateste 300
        service.inregistreazaPlata(fact, 300.0);

        // verifica daca restul este 700
        assertEquals(700.0, fact.getRestDePlata(), "Restul de plata ar trebui sa fie 700");
    }

    @Test
    void testBlocareEditareFactura() {
        // Incercam sa editam o factura care are plati
        Firma f = new Firma("Test SRL", "RO123", "Arad");
        Factura fact = new Factura(1, 1000.0, f);
        FacturareService service = new FacturareService();

        service.inregistreazaPlata(fact, 100.0);

        Exception exception = assertThrows(Exception.class, () -> {
            service.editeazaSumaFactura(fact, 2000.0);
        });

        // Verifica daca mesajul de eroare e cum ne asteptam
        assertTrue(exception.getMessage().contains("Nu se poate edita factura"));
    }
}