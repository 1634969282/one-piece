package com.mossle.javamail.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.Date;
import java.util.Properties;

import javax.annotation.Resource;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import com.mossle.javamail.persistence.domain.JavamailConfig;
import com.mossle.javamail.persistence.domain.JavamailMessage;
import com.mossle.javamail.persistence.manager.JavamailConfigManager;
import com.mossle.javamail.persistence.manager.JavamailMessageManager;
import com.mossle.javamail.support.SmtpAuthenticator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
public class JavamailService {
    private static Logger logger = LoggerFactory
            .getLogger(JavamailService.class);
    private JavamailConfigManager javamailConfigManager;
    private JavamailMessageManager javamailMessageManager;

    public Properties createSmtpProperties(JavamailConfig javamailConfig) {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol",
                javamailConfig.getSendType());
        props.setProperty("mail.smtp.host", javamailConfig.getSendHost());
        props.setProperty("mail.smtp.port", javamailConfig.getSendPort());
        props.setProperty("mail.smtp.auth", "true");

        if ("ssl".equals(javamailConfig.getSendSecure())) {
            props.setProperty("mail.smtp.ssl.enable", "true");
            props.setProperty("mail.smtp.ssl.trust",
                    javamailConfig.getSendHost());
        } else if ("ssl-all".equals(javamailConfig.getSendSecure())) {
            props.setProperty("mail.smtp.ssl.enable", "true");
            props.setProperty("mail.smtp.ssl.trust", "*");
        } else {
            logger.info("unsuppport : {}", javamailConfig.getSendSecure());
        }

        return props;
    }

    public Properties createPop3Properties(JavamailConfig javamailConfig) {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol",
                javamailConfig.getReceiveType());
        props.setProperty("mail.pop3.host", javamailConfig.getReceiveHost());
        props.setProperty("mail.pop3.port", javamailConfig.getReceivePort());

        if ("ssl".equals(javamailConfig.getReceiveSecure())) {
            props.setProperty("mail.pop3.ssl.enable", "true");
            props.setProperty("mail.pop3.ssl.trust",
                    javamailConfig.getReceiveHost());
        } else if ("ssl-all".equals(javamailConfig.getReceiveSecure())) {
            props.setProperty("mail.pop3.ssl.enable", "true");
            props.setProperty("mail.pop3.ssl.trust", "*");
        } else {
            logger.info("unsuppport : {}", javamailConfig.getReceiveSecure());
        }

        return props;
    }

    public void send(String from, String to, String cc, String bcc,
            String subject, String content) throws MessagingException {
        JavamailConfig javamailConfig = javamailConfigManager.findUniqueBy(
                "userId", from);
        this.send(to, cc, bcc, subject, content, javamailConfig);
    }

    public void send(String to, String subject, String content,
            JavamailConfig javamailConfig) throws MessagingException {
        this.send(to, null, null, subject, content, javamailConfig);
    }

    public void send(String to, String cc, String bcc, String subject,
            String content, JavamailConfig javamailConfig)
            throws MessagingException {
        logger.debug("send : {}, {}", to, subject);

        try {
            Properties props = createSmtpProperties(javamailConfig);
            String username = javamailConfig.getUsername();
            String password = javamailConfig.getPassword();

            // ??????Session????????????
            Session session = Session.getInstance(props, new SmtpAuthenticator(
                    username, password));
            session.setDebug(false);

            // ??????MimeMessage????????????
            MimeMessage message = new MimeMessage(session);
            // ??????????????????
            message.setSubject(subject);
            // ???????????????
            message.setFrom(new InternetAddress(username));
            // ??????????????????
            message.setSentDate(new Date());
            // ???????????????
            message.setRecipients(RecipientType.TO, InternetAddress.parse(to));
            // ??????html??????????????????????????????MIME?????????text/html?????????????????????????????????gbk
            message.setContent(content, "text/html;charset=gbk");

            // ????????????????????????????????????
            message.saveChanges();

            // ????????????
            Transport.send(message);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void receive(String userId) throws MessagingException, IOException {
        JavamailConfig javamailConfig = javamailConfigManager.findUniqueBy(
                "userId", userId);
        this.receive(javamailConfig);
    }

    public void receive(JavamailConfig javamailConfig)
            throws MessagingException, IOException {
        this.receivePop3(javamailConfig);
    }

    public void receivePop3(JavamailConfig javamailConfig)
            throws MessagingException, IOException {
        // ????????????????????????????????????
        Properties props = createPop3Properties(javamailConfig);

        // ??????Session????????????
        Session session = Session.getInstance(props);
        session.setDebug(false);

        Store store = session.getStore(javamailConfig.getReceiveType());
        store.connect(javamailConfig.getUsername(),
                javamailConfig.getPassword());

        Folder defaultFolder = store.getDefaultFolder();
        logger.info("default folder : {}", defaultFolder);

        this.receiveByFolder(defaultFolder, javamailConfig);

        logger.info("personal folder");

        for (Folder folder : store.getPersonalNamespaces()) {
            logger.info("personal folder : {}", folder);

            this.receiveByFolder(folder, javamailConfig);
        }

        logger.info("shared folder");

        for (Folder folder : store.getSharedNamespaces()) {
            logger.info("shared folder : {}", folder);

            this.receiveByFolder(folder, javamailConfig);
        }

        logger.info("user folder : {}", javamailConfig.getUsername());

        for (Folder folder : store.getUserNamespaces(javamailConfig
                .getUsername())) {
            logger.info("user folder : {}", folder);

            this.receiveByFolder(folder, javamailConfig);
        }

        store.close();
    }

    public void receiveByFolder(Folder folder, JavamailConfig javamailConfig)
            throws MessagingException, IOException {
        logger.info("receive : {}", folder);

        if ((Folder.HOLDS_MESSAGES & folder.getType()) != 0) {
            this.receiveMessageByFolder(folder, javamailConfig);
        }

        if ((Folder.HOLDS_FOLDERS & folder.getType()) != 0) {
            for (Folder childFolder : folder.list()) {
                this.receiveByFolder(childFolder, javamailConfig);
            }
        }

        if (folder.isOpen()) {
            // ????????????
            folder.close(false);
        }
    }

    public void receiveMessageByFolder(Folder folder,
            JavamailConfig javamailConfig) {
        try {
            /*
             * Folder.READ_ONLY??????????????? Folder.READ_WRITE????????????????????????????????????????????????
             */
            folder.open(Folder.READ_WRITE); // ???????????????

            // ??????????????????????????????
            Message[] messages = folder.getMessages();

            // ?????????????????????????????????
            logger.debug("???????????????" + messages.length + "?????????!");
            logger.debug("???????????????" + folder.getUnreadMessageCount() + "???????????????!");
            logger.debug("???????????????" + folder.getNewMessageCount() + "????????????!");
            logger.debug("???????????????" + folder.getDeletedMessageCount() + "??????????????????!");

            logger.debug("------------------------??????????????????----------------------------------");

            // ????????????
            for (Message message : messages) {
                // IMAPMessage msg = (IMAPMessage) message;
                MimeMessage mimeMessage = (MimeMessage) message;

                try {
                    if (javamailMessageManager.findUniqueBy("messageId",
                            mimeMessage.getMessageID()) != null) {
                        continue;
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);

                    continue;
                }

                String subject = this.getSubject(mimeMessage);
                logger.debug("[" + subject + "]???????????????????????????????????????yes/no??????");

                // BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                // String answer = reader.readLine();
                // String answer = "no";
                // if ("yes".equalsIgnoreCase(answer)) {
                // POP3ReceiveMailTest.parseMessage(msg); // ????????????
                // ??????????????????????????????true????????????????????????????????????false????????????????????????
                // msg.setFlag(Flag.SEEN, true); //??????????????????
                String from = this.getFrom(mimeMessage);
                logger.debug("from : " + from);

                JavamailMessage javamailMessage = new JavamailMessage();

                if (subject.length() > 255) {
                    logger.info("{} length {} larger than 255", subject,
                            subject.length());
                    subject = subject.substring(0, 255);
                }

                javamailMessage.setSubject(subject);
                javamailMessage.setSender(from);
                javamailMessage.setSendTime(mimeMessage.getSentDate());
                javamailMessage.setReceiveTime(mimeMessage.getReceivedDate());
                javamailMessage
                        .setMessageNumber(mimeMessage.getMessageNumber());
                javamailMessage.setMessageId(mimeMessage.getMessageID());
                javamailMessage.setFolder("INBOX");
                logger.debug("before content");

                StringBuffer content = new StringBuffer(30);
                getMailTextContent(message, content);
                logger.debug("content : " + content);
                javamailMessage.setContent(content.toString());
                javamailMessage.setJavamailConfig(javamailConfig);
                javamailMessageManager.save(javamailMessage);
                logger.debug("end");

                // }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public static String getSubject(MimeMessage msg)
            throws UnsupportedEncodingException, MessagingException {
        return MimeUtility.decodeText(msg.getSubject());
    }

    public static String getFrom(MimeMessage msg) throws MessagingException,
            UnsupportedEncodingException {
        String from = "";
        Address[] froms = msg.getFrom();

        if (froms.length < 1) {
            throw new MessagingException("???????????????!");
        }

        InternetAddress address = (InternetAddress) froms[0];
        String person = address.getPersonal();

        if (person != null) {
            person = MimeUtility.decodeText(person) + " ";
        } else {
            person = "";
        }

        from = person + "<" + address.getAddress() + ">";

        return from;
    }

    public void getMailTextContent(Part part, StringBuffer content)
            throws MessagingException, IOException {
        // ???????????????????????????????????????getContent????????????????????????????????????????????????????????????????????????????????????????????????
        boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;

        if (part.isMimeType("text/*") && !isContainTextAttach) {
            content.append(part.getContent().toString());
        } else if (part.isMimeType("message/rfc822")) {
            getMailTextContent((Part) part.getContent(), content);
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();

            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                getMailTextContent(bodyPart, content);
            }
        }
    }

    @Resource
    public void setJavamailMessageManager(
            JavamailMessageManager javamailMessageManager) {
        this.javamailMessageManager = javamailMessageManager;
    }

    @Resource
    public void setJavamailConfigManager(
            JavamailConfigManager javamailConfigManager) {
        this.javamailConfigManager = javamailConfigManager;
    }
}
