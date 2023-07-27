package com.learn.im.tcp;

import com.learn.im.codec.config.BootstrapConfig;
import com.learn.im.tcp.server.LeeServer;
import com.learn.im.tcp.server.LeeWebSocketServer;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author lee
 * @description
 */
public class Starter {

    public static void main(String[] args) {
//       idea设置 args = "D:\\Develop\\Mine\\IdeaProjects\\im-system\\im-tcp\\src\\main\\resources\\config.yml";
        if(args.length > 0){
            start(args[0]);
        }
    }

    private static void start(String path) {
        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(path);
            BootstrapConfig bootstrapConfig = yaml.loadAs(inputStream, BootstrapConfig.class);

            new LeeServer(bootstrapConfig.getLee()).start();
            new LeeWebSocketServer(bootstrapConfig.getLee()).start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(500);
        }
    }

}
