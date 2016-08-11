/**
 * Copyright 2016.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.tid.keyserver.controllers.db;

import java.util.Base64;
import es.tid.keyserver.core.lib.CheckObject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Database class manipulation.
 * @author <a href="mailto:jgm1986@hotmail.com">Javier Gusano Martinez</a>
 * @since v0.1.0
 */
public class DataBase implements CheckObject{
    /**
     * Redis Database Pool Object.
     */
    private static JedisPool pool;
    /**
     * Redis Database Connection Object
     */
    private Jedis dataBaseObj;
    /**
     * Redis Database address.
     */
    private final InetAddress serverIp;
    /**
     * Redis Database port. 
     */
    private final int port;
    /**
     * Redis Database password.
     */
    private final String password;
    /**
     * Flag used to check if the object is correctly connected to Redis database.
     */
    private boolean isConnected; 
    /**
     * Flag value true while trying to connect to Redis database.
     */
    private boolean connecting;
    /**
     * Logging object.
     */
    private static final Logger logger = LoggerFactory.getLogger(DataBase.class);
    
    /**
     * Main constructor of the class.
     * @param serverIp Redis server IP.
     * @param port Redis listener port.
     * @param password Redis password.
     * @since v0.3.1
     */
    public DataBase(InetAddress serverIp, int port, String password){
        // Store database connection parameters inside class attributes.
        this.serverIp = serverIp;
        this.port = port;
        this.password = password;
        // Try to connect to Redis database.
        JedisConnectionException ex = new JedisConnectionException(""); // This object is used to storage the 
                                                                        // exception during database connection.
        isConnected = connectDb(ex);
        // If the KeyServer can't connect to the Redis database.
        if(!isConnected){
            // Error level.
            logger.error("Database initialization failed.");
            // Trace level.
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            logger.trace(errors.toString());
        }
    }
    
    /**
     * Stop and close the database connection correctly.
     * @since v0.1.0
     */
    public void stop(){
        dataBaseObj.close();
        pool.close();
        pool.destroy();
    }
    
    /**
     * This class provides a basic mode to get the array of Bytes for the Private
     * key associated with the input hash certificate value.
     * @param certHash Contains the SHA1 hash of the certificate used to get private key.
     * @return Bytes array associates with the input hash value. Null if hash value is not found.
     * @since v0.1.0
     */
    public byte[] getPrivateForHash(String certHash){
        if(this.isConnected){
            String response = dataBaseObj.get(certHash);
            logger.debug("REDIS query: {} | REDIS response: {}", certHash, response);
            if (response!=null){
                // Decode from base64 to bytes and return array of values.
                return Base64.getDecoder().decode(response.trim());
            } 
        }
        return null;
    }

    /**
     * Check if the current data base object is connected to the Redis Data Base.
     * @return True if is connected, false if not.
     * @since v0.3.0
     */
    public boolean isConnected(){
        try{
            dataBaseObj.ping();
            return true;
        } catch (JedisConnectionException e){ 
            if(!this.connecting){
                connecting = true;  // Set the connecting flag True (trying to connect...).
                isConnected = connectDb(new JedisConnectionException(""));
                connecting = false; // Set the connecting flat to False (connected).
            }
            return false;
        }
    }
    
    /**
     * This class provides a basic mode to get an string with the Private
     * key codified on base64 associated with the input hash (SHA1) certificate 
     * value.
     * @param certHash Contains the SHA1 hash of the certificate used to get 
     * private key.
     * @return String associates with the input hash value. Null if hash value 
     * is not found.
     * @since v0.3.0
     */
    public String getPrivateKey(String certHash){
        if(this.isConnected){
            return dataBaseObj.get(certHash);
        }
        return null;
    }
    
    /**
     * This method insert a new PK register using hash certificate as index.
     * @param certHash SHA1 certificate hash. This field is used as index.
     * @param privKey Private key string codified as base64.
     * @return True if all works correctly. False if not.
     * @since v0.3.0
     */
    public boolean setPrivateKey(String certHash, String privKey){
        if(this.isConnected){
            dataBaseObj.set(certHash, privKey);
            String test = this.getPrivateKey(certHash);
            return test.equalsIgnoreCase(privKey);
        }
        return false;
    }
    
    /**
     * This method is used for automatic remove provisioned private keys from
     * Redis database. 
     * @param certHash SHA1 certificate hash. This field is used as index.
     * @param date Expiration date for the provided certificate.
     * @return True if all works correctly. False if not.
     * @since v0.3.1
     */
    public boolean setExpPK(String certHash, long date){
        if(this.isConnected){
            dataBaseObj.expireAt(certHash, date);
            return true;
        }
        return false;
    }
    
    /**
     * This method is used to delete an specified database index register.
     * @param certHash SHA1 hash of the certificate used as database index.
     * @return True if all works correctly. False if the specified register is 
     * not found on database.
     * @since v0.3.0
     */
    public boolean deletePrivateKey(String certHash){
        if(this.isConnected){
            if(this.getPrivateKey(certHash)!=null){
                dataBaseObj.del(certHash);
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
    
    /**
     * Get a full list with the Redis database keys whose value is equal to the 
     * pattern.
     * @param pattern Pattern use to find on database.
     * @return Full list with the results.
     * @since v0.3.0
     */
    public Set<String> getHashList(String pattern){
        if(this.isConnected){
            return dataBaseObj.keys(pattern);
        }
        return new HashSet();
    }
    
    /**
     * Return true if the object is correctly initialized or false if not.
     * @return Object initialization status.
     * @since v0.1.0
     */
    @Override
    public boolean isCorrectlyInitialized(){
        return isConnected;
    }

    /**
     * This method is used to connect this class with the Redis database.
     * @param ex This object is used to storage the possible exception object. 
     * This information is used only for log purposes. 
     * @return True if the connection has been established with the Redis 
     * database, false if not. The data description about the connection problem
     * should be storage inside input Exception object.
     * @since v0.3.1
     */
    private boolean connectDb(JedisConnectionException ex){
        pool = new JedisPool(new GenericObjectPoolConfig(), 
                serverIp.getHostAddress(), 
                port, 
                Protocol.DEFAULT_TIMEOUT, 
                password);
        try {
            // Redis connected.
            dataBaseObj = pool.getResource();
            return true;
        } catch (JedisConnectionException e) {
            ex = e; // Save the catched exception object inside input field.
            return false;
        }
    }
}
