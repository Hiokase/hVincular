package hplugins.hvincular.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Utilitário para operações com arquivos, garantindo compatibilidade
 * com todas as versões do Java, incluindo Java 8 e anteriores.
 */
public class FileUtils {

    /**
     * Cria um reader para um arquivo com encoding UTF-8,
     * compatível com todas as versões do Java.
     * 
     * @param file O arquivo a ser lido
     * @return Um Reader com encoding UTF-8
     * @throws IOException Se houver erro ao abrir o arquivo
     */
    public static Reader createUtf8Reader(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        return new InputStreamReader(fis, StandardCharsets.UTF_8);
    }
    
    /**
     * Cria um writer para um arquivo com encoding UTF-8,
     * compatível com todas as versões do Java.
     * 
     * @param file O arquivo a ser escrito
     * @return Um Writer com encoding UTF-8
     * @throws IOException Se houver erro ao abrir o arquivo
     */
    public static Writer createUtf8Writer(File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        return new OutputStreamWriter(fos, StandardCharsets.UTF_8);
    }
}