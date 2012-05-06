/*
 *   Copyright 2009-2012 Joubin Houshyar
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

import java.util.Map;

/**
 * RedisInfo is a convenience enum that tracks the canonical entries returned by INFO command.
 * Exception to this is the db## when ## is db number e.g. db10.  
 * <b>
 * Note that of course the {@link JRedis#info()} command returns a {@link Map} so you can always
 * query for the db info directly using the returned map.
 * 
 * @author  Joubin Houshyar (alphazero@sensesay.net)
 * @version alpha.0-05062012
 * @since   alpha.0
 * 
 */
public enum RedisInfo {
	redis_git_dirty,
	redis_git_sha1,
	used_cpu_user,
	client_longest_output_list,
	latest_fork_usec,
	used_memory_peak,
	pubsub_patterns,
	used_cpu_sys_children,
	used_memory_rss,
	lru_clock,
	aof_enabled,
	mem_fragmentation_ratio,
	used_memory_peak_human,
	loading,
	client_biggest_input_buf,
	used_cpu_sys,
	keyspace_hits,
	evicted_keys,
	pubsub_channels,
	used_cpu_user_children,
	gcc_version,
	expired_keys,
	mem_allocator,
	keyspace_misses,
	arch_bits,
	multiplexing_api,
	redis_version,
	process_id,
	connected_clients,
	connected_slaves,
	blocked_clients,
	used_memory,
	used_memory_human,
	changes_since_last_save,
	bgsave_in_progress,
	last_save_time,
	bgrewriteaof_in_progress,
	total_connections_received,
	total_commands_processed,
	uptime_in_seconds,
	uptime_in_days,
//	hash_max_zipmap_entries,
//	hash_max_zipmap_value,
//	vm_enabled,
	os,
	tcp_port,
	used_memory_lua,
	last_bgsave_status,
	instantaneous_ops_per_sec,
	rejected_connections,
	run_id,
	bgrewriteaof_scheduled,	
	role
}