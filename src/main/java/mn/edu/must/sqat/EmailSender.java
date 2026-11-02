package mn.edu.must.sqat;

/** И-мэйл илгээгч — хэрэгжүүлэлт ЗОРИУД байхгүй. */
public interface EmailSender {
    void send(String to, String subject, String body);
}
