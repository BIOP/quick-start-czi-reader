package ch.epfl.biop.formats.in;

public class OpenFile {
    public static void main(String... args) throws Exception {
        ZeissQuickStartCZIReader reader = new ZeissQuickStartCZIReader();
        reader.setId("C:/Users/nicol/Desktop/test_gray.czi");
    }
}
