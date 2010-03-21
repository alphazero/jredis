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

package org.jredis;

/**
 * Enumeration of elements of the structured information returned by {@link JRedis#info()} and {@link JRedisFuture#info}.
 * @Redis INFO
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0, 04/02/09
 * @since   alpha.0
 * @see JRedis
 */
public enum RedisInfo {
	redis_version,
	connected_clients,
	connected_slaves,
	used_memory,
	changes_since_last_save,
	last_save_time,
	total_connections_received,
	total_commands_processed,
	arch_bits,
	multiplexing_api,
	used_memory_human,
	bgsave_in_progress,
	bgrewriteaof_in_progress,
	role,
	uptime_in_seconds,
	uptime_in_days
}
/* missing in action
db0:keys=1,expires=0
*/