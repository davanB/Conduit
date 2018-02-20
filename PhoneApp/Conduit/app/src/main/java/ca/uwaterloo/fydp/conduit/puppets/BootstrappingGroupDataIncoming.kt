package ca.uwaterloo.fydp.conduit.puppets

import com.conduit.libdatalink.ConduitGroup
import com.conduit.libdatalink.conduitabledata.ConduitConnectionEvent
import com.conduit.libdatalink.conduitabledata.ConduitGroupData
import com.conduit.libdatalink.conduitabledata.ConduitableDataTypes

class BootstrappingGroupDataIncoming(group: ConduitGroup) : PuppetShow(group) {
    override fun writeScript(){
        script.clear()
        delay(5000)
        val groupMembers: ArrayList<String> = ArrayList()
        groupMembers.add("Master Baytes")
        groupMembers.add("Bob")
        groupMembers.add("Joe")
        groupMembers.add("Smith")
        groupMembers.add("Fred")
        groupMembers.add("Menma")

        val groupData: ConduitGroupData =  ConduitGroupData("Cool guys", 6, groupMembers)
        script.add { group.conduitableListeners[ConduitableDataTypes.GROUP_DATA.flag]?.invoke(groupData)}
    }

}