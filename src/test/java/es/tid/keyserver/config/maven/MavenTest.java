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
package es.tid.keyserver.config.maven;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test class for "Maven" properties management class.
 * @author <a href="mailto:jgm1986@hotmail.com">Javier Martinez Gusano</a>
 * @since v0.3.0
 */
public class MavenTest {
    /**
     * Private test object for MavenTest class.
     */
    private final Maven testObj;
    
    /**
     * Test class constructor.
     */
    public MavenTest() {
        testObj = new Maven("/properties/applicationtest.properties");
    }

    /**
     * Fail loading file
     * @since v0.4.2
     */
    @Test
    public void failLoadingFile(){
        System.out.println("getVersion");
        try{
            Maven testFail = new Maven("/properties/filenotfound.properties");
            testFail.isCorrectlyInitialized();
        }catch(Exception e){
            fail("'Loading not valid file' test failed. ");
        }
    }
    /**
     * Test of getVersion method, of class Maven.
     * @since v0.3.0
     */
    @Test
    public void testGetVersion() {
        System.out.println("getVersion");
        String expResult = "v1.0.1.test";
        String result = testObj.getVersion();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppName method, of class Maven.
     * @since v0.3.0
     */
    @Test
    public void testGetAppName() {
        System.out.println("getAppName");
        String expResult = "Test App Name";
        String result = testObj.getAppName();
        assertEquals(expResult, result);
    }

    /**
     * Test of isCorrectlyInitialized method, of class Maven.
     */
    @Test
    public void testIsCorrectlyInitialized() {
        System.out.println("isCorrectlyInitialized");
        boolean expResult = true;
        boolean result = testObj.isCorrectlyInitialized();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectPublicUrl method, of class Maven.
     */
    @Test
    public void testGetProjectPublicUrl() {
        System.out.println("getProjectPublicUrl");
        String expResult = "https://github.com/mami-project/KeyServer";
        String result = testObj.getProjectPublicUrl();
        assertEquals(expResult, result);
    }
}
