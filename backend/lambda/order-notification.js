/**
 * Lambda Function: Order Notification
 * 
 * Esta Lambda se ejecuta cuando una orden es procesada exitosamente.
 * Envía una notificación al cliente via email/SMS.
 * 
 * Trigger: SNS Topic "order-completed"
 * 
 * @author MS-Event-Processor Team
 * @version 1.0.0
 */

exports.handler = async (event) => {
    console.log('🔔 Lambda Order Notification ejecutada');
    console.log('Event:', JSON.stringify(event, null, 2));
    
    // Procesar cada registro del evento SNS
    for (const record of event.Records || []) {
        try {
            const snsMessage = record.Sns;
            const message = JSON.parse(snsMessage.Message);
            
            console.log('📧 Procesando notificación para orden:', message.orderId);
            
            // Simular envío de email
            const emailContent = {
                to: message.customerEmail || 'customer@example.com',
                subject: `Orden ${message.orderId} - ${message.status}`,
                body: `
                    Hola ${message.customerName || 'Cliente'},
                    
                    Tu orden #${message.orderId} ha sido ${message.status}.
                    
                    Detalles:
                    - Total: $${message.totalAmount}
                    - Estado: ${message.status}
                    - Fecha: ${new Date().toISOString()}
                    
                    Gracias por tu compra!
                    
                    MS-Event-Processor Team
                `
            };
            
            console.log('📧 Email simulado enviado:', JSON.stringify(emailContent, null, 2));
            
            // Aquí iría la integración real con AWS SES, SendGrid, etc.
            
        } catch (error) {
            console.error('❌ Error procesando mensaje:', error);
        }
    }
    
    return {
        statusCode: 200,
        body: JSON.stringify({
            message: 'Notificaciones procesadas exitosamente',
            processedRecords: event.Records?.length || 0
        })
    };
};
