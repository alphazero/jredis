/*
 *   Copyright 2009 Joubin Houshyar
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *    
 *   http://www.apache.org/licenses/LICENSE-2.0
 *    
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

/**
 * This package is optional.  Its entirely debatable whether such mechanisms
 * have any legitimate place in JRedis, which is a connector to a type-less
 * system storing blobs of bytes.  At this intial point, its place here simply
 * serves the purpose of keeping the issues that this package attempts to address
 * in mind.
 * <p>
 * When all is said and done, all these artifacts will provide little more than
 * what a helper or wrapper object will provide -- JRedis can not be in the
 * type management business, as it does not control the object store.
 */
package org.jredis.semantics;