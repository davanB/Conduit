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
        groupMembers.add("Aaron")
        groupMembers.add("Alvin")
        groupMembers.add("Navjot")
        groupMembers.add("Shadmaan")
        groupMembers.add("Davan")
        groupMembers.add("Joe")

        val groupData: ConduitGroupData =  ConduitGroupData("The Boys", 6, groupMembers)
        script.add { group.conduitableListeners[ConduitableDataTypes.GROUP_DATA.flag]?.invoke(groupData)}
    }

}