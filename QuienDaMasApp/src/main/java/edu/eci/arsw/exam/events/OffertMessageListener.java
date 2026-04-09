package edu.eci.arsw.exam.events;

import edu.eci.arsw.exam.IdentityGenerator;
import edu.eci.arsw.exam.Product;
import edu.eci.arsw.exam.remote.ManejadorOfertasStub;
import java.util.Random;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;

public class OffertMessageListener implements MessageListener {

    Random rand = new Random(System.currentTimeMillis());
    private ManejadorOfertasStub manejadorOfertasStub;

    public void setManejadorOfertasStub(ManejadorOfertasStub manejadorOfertasStub) {
        this.manejadorOfertasStub = manejadorOfertasStub;
    }

    public OffertMessageListener() {
        super();
        System.out.println("Comprador"+IdentityGenerator.actualIdentity+"esta esperando eventos");
    }

    @Override
    public void onMessage(Message message) {
        try {
            Object payload = deserialize(message.getBody());

            if (payload instanceof Product) {
                Product receivedProduct = (Product) payload;
                System.out.println("Comprador #" + IdentityGenerator.actualIdentity + " recibió: " + receivedProduct.getCode());

                int montoOferta = receivedProduct.setStartPrice() + rand.nextInt(100000);
                manejadorOfertasStub.agregarOferta(IdentityGenerator.actualIdentity, receivedProduct.getCode(), montoOferta);
            }
            else if (payload instanceof SaleNotification) {
                SaleNotification saleNotification = (SaleNotification) payload;
                if (IdentityGenerator.actualIdentity.equals(saleNotification.getBuyerId())) {
                    System.out.println("Comprador " + saleNotification.getBuyerId() + " Producto " + saleNotification.getProductCode());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("An exception occured while trying to get a AMQP object:" + e.getMessage(), e);
        }

    }

    private Object deserialize(byte[] body) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(body);
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object obj = ois.readObject();
        ois.close();
        bis.close();
        return obj;
    }

}
