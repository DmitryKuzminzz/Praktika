package com.example.sweater;

import com.example.sweater.domain.Message;
import com.example.sweater.repos.MessageRep;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class UploadController
{

@Autowired
private MessageRep messageRep;
    @Value("${upload.path}")
    private String uploadPath;
    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Map<String,Object> model) {
        model.put("name", name);
        return "greeting";
    }
    @GetMapping
    public String main(Map<String,Object> model) {
        Iterable<Message> messages=messageRep.findAll();
        model.put("repo",messages);
        return "main";
    }
    @PostMapping
    public String add(
             @RequestParam String autor, Map<String,Object> model,
             @RequestParam("file") MultipartFile file) throws IOException {
        Message message =new Message(null,autor,null,null,null,null);
        String resu="";
        if (file!=null)
        {
            File uploadDir= new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
        }
        String res=file.getOriginalFilename();
            file.transferTo(new File(uploadPath + "/" + res));
            message.setDocname(res);
            String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            message.setDat(date);
            String dateupdate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            message.setDat(dateupdate);
            message.setUploaddate(dateupdate);
            String pop = tessract(uploadPath + "/" + res);
            message.setText(pop);
            String key = keyw(pop);
            message.setKeywords(key);
            messageRep.save(message);
            Iterable<Message> messages = messageRep.findAll();
            model.put("messages", messages);
        return "main";
    }


    public static String keyw(String tes){
        StringBuilder as= new StringBuilder();
            HashMap<String, Integer> map = new HashMap<>();
            for (int i = 0; i < tes.length(); i++) {
                if ((Character.isLetter(tes.charAt(i))) || (Character.isDigit(tes.charAt(i)))) {
                    as.append(tes.charAt(i));
                } else {
                    as.append(" ");
                }
            }
            StringTokenizer st = new StringTokenizer(as.toString());
            while (st.hasMoreTokens()) {
                String z = st.nextToken();
                if (map.containsKey(z)) {
                    map.put(z, map.get(z) + 1);
                } else if (z.length() >= 5) {
                    map.put(z, 1);
                }
            }
            Map<String, Integer> sortedMap = sortByValue(map);
            String output = "";
            int o = 0;
            for (String key : sortedMap.keySet()) {
                if (o < 7) {
                    output += key + " ";
                    o++;
                } else break;
            }
            as = new StringBuilder(output);
        return as.toString();
    }
public static String tessract(String url)
{
    String ret="";
    Tesseract tesseract = new Tesseract();
    try {
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
        String q = tesseract.doOCR(new File(url));
        ret=q;
        } catch (TesseractException e) {
        e.printStackTrace();
    }
    return ret;
}
    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {

        // 1. Convert Map to List of Map
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // 2. Sort list with Collections.sort(), provide a custom Comparator
        //    Try switch the o1 o2 position for a different order
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>()
        {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }


        return sortedMap;
    }

}