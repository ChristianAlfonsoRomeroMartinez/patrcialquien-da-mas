/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.exam.remote;

import edu.eci.arsw.exam.FachadaPersistenciaOfertas;
import edu.eci.arsw.exam.Product;
import edu.eci.arsw.exam.events.OffertMessageProducer;
import edu.eci.arsw.exam.events.SaleNotification;

/**
 *
 * @author hcadavid
 */
public class ManejadorOfertasSkeleton implements ManejadorOfertasStub{

    private FachadaPersistenciaOfertas fpers=null;
    private OffertMessageProducer messageProducer;
    private WinnerAnnouncer winnerAnnouncer;

    public void setFachadaPersistenciaOfertas(FachadaPersistenciaOfertas fpers) {
        this.fpers = fpers;
    }

    public void setMessageProducer(OffertMessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    public void setWinnerAnnouncer(WinnerAnnouncer winnerAnnouncer) {
        this.winnerAnnouncer = winnerAnnouncer;
    }
            
    @Override
    public void agregarOferta(String codOferente,String codprod,int monto) {
        Object lock = fpers.getBloqueoProducto(codprod);
        synchronized (lock) {
            Integer ofertasRecibidas = fpers.getMapaOfertasRecibidas().get(codprod);
            if (ofertasRecibidas != null && ofertasRecibidas >= 3) {
                return;
            }

            Product producto = fpers.getMapaProductosSolicitados().get(codprod);
            if (producto == null) {
                return;
            }

            if (monto < producto.setStartPrice()) {
                return;
            }

            int nuevasOfertas = (ofertasRecibidas == null ? 0 : ofertasRecibidas) + 1;
            fpers.getMapaOfertasRecibidas().put(codprod, nuevasOfertas);

            Integer mejorMontoActual = fpers.getMapaMontosAsignados().get(codprod);
            if (mejorMontoActual == null || monto > mejorMontoActual) {
                fpers.getMapaMontosAsignados().put(codprod, monto);
                fpers.getMapaOferentesAsignados().put(codprod, codOferente);
            }

            if (nuevasOfertas == 3) {
                String ganador = fpers.getMapaOferentesAsignados().get(codprod);
                Integer montoGanador = fpers.getMapaMontosAsignados().get(codprod);

                if (winnerAnnouncer != null) {
                    winnerAnnouncer.announceWinner(codprod, ganador, montoGanador);
                }

                if (messageProducer != null) {
                    messageProducer.sendMessages("my.sale." + ganador, new SaleNotification(ganador, codprod));
                }
            }
        }
    }

    public static interface WinnerAnnouncer {
        void announceWinner(String codProducto, String codComprador, Integer monto);
    }

}
