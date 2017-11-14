package pl.edu.agh.littledropbox.gui;

import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by janwojcik5 on 2017-05-22.
 */
public class LoginWindow extends JFrame {

        private JLabel userLabel=new JLabel("user");
        private JLabel passwordLabel=new JLabel("password");
        private JTextField userField=new JTextField();
        private JPasswordField passwordField=new JPasswordField();
        private JButton loginButton=new JButton("login");
        private JButton cancelButton=new JButton("cancel"); //can be changed to register button

        private CookieStore cookieStore=new BasicCookieStore();
        private CloseableHttpClient httpClient= HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        private Properties prop=new Properties();

        public LoginWindow() {
                super("login");
                setSize(300,150);
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                JPanel panel=new JPanel();
                add(panel);
                placeComponents(panel);

                try {
                        InputStream inputStream=this.getClass().getResourceAsStream("/config.properties");
                        if(inputStream==null) {
                                throw new IOException("Config file not present.");
                        }
                        prop.load(inputStream);
                } catch(IOException ioe) {
                        ioe.printStackTrace();
                }

                loginButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                                HttpPost httpPost=new HttpPost(prop.getProperty("server")+"login");
                                List<NameValuePair> nvps=new ArrayList<NameValuePair>();
                                nvps.add(new BasicNameValuePair("username",userField.getText()));
                                nvps.add(new BasicNameValuePair("password",String.valueOf(passwordField.getPassword())));
                                try {
                                        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
                                } catch(UnsupportedEncodingException ue) {
                                        ue.printStackTrace();
                                }
                                try(CloseableHttpResponse response=httpClient.execute(httpPost)) {
                                        //cookies are now stored in cookieStore
                                } catch(Exception exc) {
                                        exc.printStackTrace();
                                }
                                UserFilesView filesView=new UserFilesView(cookieStore);

                        }
                });

                cancelButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {

                        }
                });

                setVisible(true);
        }

        private void placeComponents(JPanel panel) {
                panel.setLayout(null);

                userLabel.setBounds(10,10,80,25);
                panel.add(userLabel);

                userField.setBounds(100,10,160,25);
                panel.add(userField);

                passwordLabel.setBounds(10,40,80,25);
                panel.add(passwordLabel);

                passwordField.setBounds(100,40,160,25);
                panel.add(passwordField);

                loginButton.setBounds(10,80,80,25);
                panel.add(loginButton);

                cancelButton.setBounds(180,80,80,25);
                panel.add(cancelButton);
        }

}
