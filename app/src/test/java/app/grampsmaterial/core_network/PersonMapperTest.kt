package app.grampsmaterial.core_network

import app.grampsmaterial.core_network.models.GrampsEventRef
import app.grampsmaterial.core_network.models.GrampsName
import app.grampsmaterial.core_network.models.GrampsPerson
import app.grampsmaterial.core_network.models.GrampsSurname
import app.grampsmaterial.core_network.models.displayName
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonMapperTest {
    @Test fun `display name keeps a person with incomplete name data visible`() {
        val person = GrampsPerson(handle = "P1", primary_name = GrampsName(first_name = null, surname_list = listOf(GrampsSurname(surname = "Example"))))
        assertEquals("Example", person.displayName())
    }

    @Test fun `display name has a neutral fallback`() {
        assertEquals("Unknown person", GrampsPerson(handle = "P1").displayName())
    }

    @Test fun `event reference is not displayed as a birth date`() {
        val person = GrampsPerson(handle = "P1", event_ref_list = listOf(GrampsEventRef("opaque-event-handle")))
        assertEquals(null, person.birthDate)
    }
}
