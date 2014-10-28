/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.mico.marmotta.api;

import org.openrdf.model.URI;

/**
 * Add file description here!
 *
 * @author Sebastian Schaffert (sschaffert@apache.org)
 */
public interface ContextLockingService {

    /**
     * Lock the context with the given URI for reading using a cluster-wide semaphore and lock. If the current
     * process holds a read-lock, there can be any number of additional read-locks by other processes, but
     * no write locks.
     *
     * @param context
     */
    public void lockContextsRead(URI... context);

    /**
     * Lock the context with the given URI for writing using a cluster-wide semaphore and lock. If the current
     * process holds a write lock, there can be no other read-locks or write-locks.
     * @param context
     */
    public void lockContextWrite(URI context);


    /**
     * Release the read lock held by the current process, and signal waiting writers in case there are no
     * more read locks.
     *
     * @param context
     */
    public void unlockContextsRead(URI... context);


    /**
     * Release the write lock held by the current process and signal waiting readers and writers.
     * @param context
     */
    public void unlockContextWrite(URI context);
}
