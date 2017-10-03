/**
 * Copyright 2017.
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

package es.tid.keyserver.https.jetty.exceptions;

/**
 * Class for custom KeyServer exception.
 * @author <a href="mailto:jgm1986@hotmail.com">Javier Martinez Gusano</a>
 * @since v0.4.2
 */
public class KeyServerException extends Exception{
    /**
     * KeyServer Exception constructor.
     * @param msg Error message. 
     *     This errors has been defined inside:
     *     es.tid.keyserver.https.protocol.ErrorJSON.java
     * @since v0.4.2
     */
    public KeyServerException(String msg) {
        super(msg);
    }
}
