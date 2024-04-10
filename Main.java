import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Main {
    public static void main(String[] args) {
        Thread thread1 = new Thread(new ReadStudentInfoThread());
        thread1.start();
    }

    static class ReadStudentInfoThread implements Runnable {
        @Override
        public void run() {
            try {
                File file = new File("student.xml");
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);

                NodeList studentNodes = document.getElementsByTagName("Student");

                Document resultDocument = builder.newDocument();
                Element rootElement = resultDocument.createElement("Students");
                resultDocument.appendChild(rootElement);

                for (int i = 0; i < studentNodes.getLength(); i++) {
                    Element studentElement = (Element) studentNodes.item(i);

                    String id = studentElement.getAttribute("id");
                    String name = studentElement.getAttribute("name");
                    String address = studentElement.getAttribute("address");
                    String dateOfBirthStr = studentElement.getAttribute("dateOfBirth");
                    LocalDate dateOfBirth = null;
                    if (dateOfBirthStr != null && !dateOfBirthStr.isEmpty()) {
                        dateOfBirth = LocalDate.parse(dateOfBirthStr);
                    }

                    studentElement = resultDocument.createElement("Student");
                    studentElement.setAttribute("id", id != null ? id : "");
                    studentElement.setAttribute("name", name != null ? name : "");
                    studentElement.setAttribute("address", address != null ? address : "");

                    if (dateOfBirth == null) {
                        studentElement.setAttribute("age", "");
                        studentElement.setAttribute("encodedAge", "");
                        studentElement.setAttribute("isPrime", "false");
                        rootElement.appendChild(studentElement);
                        continue;
                    }

                    Thread thread2 = new Thread(new CalculateAgeAndEncodeThread(id, name, address, dateOfBirth, studentElement, resultDocument));
                    thread2.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class CalculateAgeAndEncodeThread implements Runnable {
        private String id;
        private String name;
        private String address;
        private LocalDate dateOfBirth;
        private Element studentElement;
        private Document resultDocument;

        public CalculateAgeAndEncodeThread(String id, String name, String address, LocalDate dateOfBirth, Element studentElement, Document resultDocument) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.dateOfBirth = dateOfBirth;
            this.studentElement = studentElement;
            this.resultDocument = resultDocument;
        }

        @Override
        public void run() {
            try {

                String age = calculateAge(dateOfBirth);

                String encodedAge = encodeAge(age);

                String decodedAge = decodeAge(encodedAge);

                studentElement.setAttribute("age", decodedAge);

                Thread thread3 = new Thread(new CheckPrimeNumberThread(id, name, address, age, encodedAge, studentElement, resultDocument));
                thread3.start();
            } catch (DateTimeParseException e) {
                e.printStackTrace();
            }
        }

        private String calculateAge(LocalDate dateOfBirth) {
            try {
                LocalDate currentDate = LocalDate.now();
                Period period = Period.between(dateOfBirth, currentDate);

                int years = period.getYears();
                int months = period.getMonths();
                int days = period.getDays();

                String age;

                if (years >= 18) {
                    age = "Adult";
                } else
                    if (years >= 12) {
                        age = "Teenager";
                    } else {
                        age = "Child";
                    }

                return age;
            } catch (DateTimeParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        private String encodeAge(String age) {
            String encodedAge = "";

            for (int i = 0; i < age.length(); i++) {
                char c = age.charAt(i);
                int asciiValue = (int) c;
                encodedAge += Integer.toString(asciiValue);
            }

            return encodedAge;
        }

        private String decodeAge(String encodedAge) {
            String decodedAge = "";

            for (int i = 0; i < encodedAge.length(); i += 2) {
                // Lấy hai chữ số liên tiếp trong encodedAge
                String digitStr = encodedAge.substring(i, i + 2);

                int asciiValue = Integer.parseInt(digitStr);

                char c = (char) asciiValue;

                decodedAge += c;
            }

            return decodedAge;
        }
    }

    static class CheckPrimeNumberThread implements Runnable {
        private String id;
        private String name;
        private String address;
        private String age;
        private String encodedAge;
        private Element studentElement;
        private Document resultDocument;

        public CheckPrimeNumberThread(String id, String name, String address, String age, String encodedAge, Element studentElement, Document resultDocument) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.age = age;
            this.encodedAge = encodedAge;
            this.studentElement = studentElement;
            this.resultDocument = resultDocument;
        }

        @Override
        public void run() {

            boolean isPrime = isPrimeNumber(encodedAge);

            studentElement.setAttribute("isPrime", Boolean.toString(isPrime));

            resultDocument.getDocumentElement().appendChild(studentElement);

            if (resultDocument.getDocumentElement().getChildNodes().getLength() == studentElement.getParentNode().getChildNodes().getLength()) {
                saveResultDocument(resultDocument);
            }
        }

        private boolean isPrimeNumber(String number) {
            try {
                int num = Integer.parseInt(number);

                if (num < 2) {
                    return false;
                }

                for (int i = 2; i <= Math.sqrt(num); i++) {
                    if (num % i == 0) {
                        return false;
                    }
                }

                return true;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return false;
            }
        }

        private void saveResultDocument(Document document) {
            try {
                File file = new File("kq.xml");
                FileOutputStream outputStream = new FileOutputStream(file);

                javax.xml.transform.TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
                javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();
                javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(document);
                javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(outputStream);
                transformer.transform(source, result);

                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}