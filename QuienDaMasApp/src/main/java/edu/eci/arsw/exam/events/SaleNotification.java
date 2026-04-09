package edu.eci.arsw.exam.events;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SaleNotification implements Serializable {

    private String buyerId;
    private String productCode;

    public SaleNotification(String buyerId, String productCode) {
        this.buyerId = buyerId;
        this.productCode = productCode;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public String getProductCode() {
        return productCode;
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(this);
        oos.flush();
        oos.reset();
        byte[] bytes = baos.toByteArray();
        oos.close();
        baos.close();
        return bytes;
    }

    public SaleNotification(byte[] body) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(body);
        ObjectInputStream ois = new ObjectInputStream(bis);
        SaleNotification obj = (SaleNotification) ois.readObject();
        ois.close();
        bis.close();
        this.buyerId = obj.buyerId;
        this.productCode = obj.productCode;
    }
}