package com.sabikudi.shared

import kotlinx.serialization.Serializable

@Serializable
enum class Role { OWNER, ORGANISER, STAFF, MEMBER }

@Serializable
enum class MemberStatus { INVITED, PENDING, ACTIVE, SUSPENDED, LOCKED, REMOVED }

/** One mode, one job: BUSINESS is for stock, SHARED is for splitting and settling. */
@Serializable
enum class WorkspaceMode { BUSINESS, SHARED }

/**
 * Result of the server-side Play Integrity check for a device.
 * UNVERIFIED means the check ran and failed. UNKNOWN means it could not run (timeout, no Play Services).
 * Neither blocks joining.
 */
@Serializable
enum class DeviceIntegrity { VERIFIED, UNVERIFIED, UNKNOWN }
