package pe.utp.eventos.registration.service.mail;

public interface EmailSender {
    void send(String to, String subject, String text);
}
