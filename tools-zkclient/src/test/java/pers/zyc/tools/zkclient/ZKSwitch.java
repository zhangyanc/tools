package pers.zyc.tools.zkclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 控制启动和关闭zk服务
 *
 * @author zhangyancheng
 */
class ZKSwitch {
    private String zkBaseDir;
    private int zkPort;
    private int zkPid = -1;

    ZKSwitch(String zkBaseDir, int zkPort) {
        this.zkBaseDir = zkBaseDir;
        this.zkPort = zkPort;
    }

    private Process exec(String cmd) {
        try {
            return Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private int getZooKeeperPid() {
        Process process = exec("netstat -ano");
        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "GBK"));

            String line;
            while ((line = bufferedReader.readLine()) != null){
                if (line.contains(":" + zkPort)) {
                    //[, TCP, 0.0.0.0:2181, 0.0.0.0:0, LISTENING, 409792]
                    String[] array = line.split("\\s+");
                    return Integer.parseInt(array[array.length - 1]);
                }
            }
            throw new RuntimeException("Can not find zookeeper pid!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void closeZooKeeper(int zkWinPid) {
        exec("taskkill /f /pid " + zkWinPid);
    }

    void open() {

        zkPid = getZooKeeperPid();
    }

    void close() {
        if (zkPid < 0) {
            throw new RuntimeException("ZooKeeper not started!");
        }
        exec("taskkill /f /pid " + zkPid);
        zkPid = -1;
    }
}
