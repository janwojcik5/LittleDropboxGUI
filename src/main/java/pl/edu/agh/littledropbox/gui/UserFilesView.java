package pl.edu.agh.littledropbox.gui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by oem on 2017-05-26.
 */
public class UserFilesView extends JFrame {
    private JScrollPane treeScrollPane=new JScrollPane();
    private JTree fileTree;
    private JTextArea textArea;
    private JButton addFileButton;
    private JButton removeFileButton;

    private CookieStore cookieStore;
    private CloseableHttpClient httpClient;

    private Properties props=new Properties();

    private static class FileNode {
        private String path;
        private long size;

        public FileNode(String path,long size) {
            this.path=path;
            this.size=size;
        }

        public String getPath() {return path;}

        public long getSize() {return size;}

        public String toString() {
            return path+" [size:"+size+"]";
        }
    }

    public UserFilesView(CookieStore cookieStore) {
        super("Your files");
        setSize(500,400);

        this.cookieStore=cookieStore;
        httpClient= HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();

        try {
            props.load(this.getClass().getResourceAsStream("/config.properties"));
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        fileTree=new JTree(constructTree());
        //add the behavour for dragging and dropping files (may be also copy/paste)
        fileTree.setTransferHandler(new TransferHandler() {

        });


        //Component contentPane=getContentPane();

        add(fileTree,BorderLayout.PAGE_START);

        JPanel buttonPanel=new JPanel();
        addFileButton=new JButton("Add File");
        removeFileButton=new JButton("RemoveFile");
        buttonPanel.add(addFileButton,BorderLayout.LINE_START);
        buttonPanel.add(removeFileButton,BorderLayout.LINE_END);
        add(buttonPanel,BorderLayout.PAGE_END);

        addFileButton.addActionListener((e) -> {
            JFileChooser chooser=new JFileChooser();
            int retval=chooser.showOpenDialog(UserFilesView.this);
            if(retval==JFileChooser.APPROVE_OPTION) {

                File file = chooser.getSelectedFile();
                HttpPost httpPost = new HttpPost(props.getProperty("server") + "uploadfile");
                FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);

                //retrieve full file name (with directory)
                TreePath selectedFilePath = fileTree.getSelectionPath();
                //if nothing is selected
                //if(selectedFilePath==null) {
                //
                //}
                //construct file path string
                StringBuilder sb = new StringBuilder();

                if (!fileTree.isSelectionEmpty()) {
                    for (Object node : selectedFilePath.getPath()) {
                        DefaultMutableTreeNode nodeAsTreeNode = (DefaultMutableTreeNode) node;
                        sb.append(nodeAsTreeNode.toString()).append("/");
                    }
                }


                String fileName=sb.toString()+file.getName();

                HttpEntity entity=MultipartEntityBuilder.create()
                        //.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                        //.addBinaryBody("file",fileBody)
                        //.addPart("filename",fileName)
                        .addTextBody("filename",fileName)
                        .addBinaryBody("file",file)
                        .build();
                httpPost.setEntity(entity);

                try(CloseableHttpResponse response=httpClient.execute(httpPost)) {
                    if(response.getStatusLine().getStatusCode()!=200) {
                        response.getEntity().writeTo(System.err);
                        throw new Exception("Request uploadfile returned status code "+response.getStatusLine().getStatusCode());
                    }
                } catch(Exception exc) {
                    exc.printStackTrace();
                }

                //refresh tree content
                remove(fileTree);
                fileTree=new JTree(constructTree());
                add(fileTree,BorderLayout.PAGE_START);
                revalidate();
                repaint();
            }
        });
        removeFileButton.addActionListener((e) -> {
            if(!fileTree.isSelectionEmpty() && fileTree.getSelectionPath().getPathCount()>0) {
                HttpPost httpPost=new HttpPost(props.getProperty("server")+"deletefile");

                StringBuilder sb=new StringBuilder();
                for(Object node:fileTree.getSelectionPath().getPath()) {
                    DefaultMutableTreeNode nodeAsTreeNode=(DefaultMutableTreeNode)node;
                    sb.append(nodeAsTreeNode.toString()).append("/");
                }

                System.out.println(sb.toString());
                System.out.println(sb.toString());
                String fileName=sb.toString().replaceAll(" \\[size: [0-9]+]/$","");//.replaceAll("^.+/","");
                System.out.println(fileName);
                System.out.println(fileName);
                System.out.flush();

                HttpEntity entity=MultipartEntityBuilder.create()
                        .addTextBody("filename",fileName)
                        .build();
                httpPost.setEntity(entity);

                try(CloseableHttpResponse response=httpClient.execute(httpPost)) {
                    if(response.getStatusLine().getStatusCode()!=200) {
                        response.getEntity().writeTo(System.err);
                        throw new Exception("Request uploadfile returned status code "+response.getStatusLine().getStatusCode());
                    }
                } catch(Exception exc) {
                    exc.printStackTrace();
                }

                remove(fileTree);
                fileTree=new JTree(constructTree());
                add(fileTree,BorderLayout.PAGE_START);
                revalidate();
                repaint();

            }
        });
        setVisible(true);
    }

    private DefaultMutableTreeNode constructTree() {
        DefaultMutableTreeNode root=new DefaultMutableTreeNode("files");
        HttpGet httpGet=new HttpGet(props.getProperty("server")+"seestructure");
        try(CloseableHttpResponse response=httpClient.execute(httpGet)) {
            if(response.getStatusLine().getStatusCode()!=200)
                throw new Exception("Request seeStructure returned status code "+response.getStatusLine().getStatusCode());
            ObjectMapper mapper=new ObjectMapper();
            JsonNode rootNode=mapper.readTree(response.getEntity().getContent());
            constructTreeRecursive(root,rootNode);

        } catch(Exception exc) {
            exc.printStackTrace();
        }
        return root;
    }

    private void constructTreeRecursive(DefaultMutableTreeNode treeNode,JsonNode jsonNode) {
        treeNode.setUserObject(new FileNode(jsonNode.get("path").asText(),jsonNode.get("size").asLong()));
        JsonNode children=jsonNode.get("children");
        if(children!=null) {
            for (JsonNode childJsonNode : children) {
                DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode();
                treeNode.add(childTreeNode);
                constructTreeRecursive(childTreeNode, childJsonNode);
            }
        }
    }


}
