package ca.uwaterloo.fydp.conduit.connectionutils


class ConduitLedger(val groupAddress: Int, val groupName: String, val groupSize: Int, val currentUserId: Int , val currentUserName: String) {
    private val groupMembers: HashMap<Int, String> = hashMapOf()

    init {
        groupMembers[currentUserId] = currentUserName
    }

    fun addGroupMember(id: Int, name: String) {
        groupMembers[id] = name
    }

    fun getUserNameForId(id: Int): String {
        return groupMembers[id] ?: "Unknown"
    }

    fun getGroupMemberNamesList(): List<String> {
        val res: ArrayList<String> = ArrayList()
        for (i in 0..groupSize) {
            groupMembers[i]?.let { res.add(it) }
        }
        return res
    }
}
