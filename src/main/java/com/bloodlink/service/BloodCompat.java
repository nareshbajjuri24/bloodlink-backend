package com.bloodlink.service;

import java.util.List;
import java.util.Map;

/** Which donor blood groups can give to a patient of a given group. */
public final class BloodCompat {
    private BloodCompat() {}

    private static final Map<String, List<String>> DONORS_FOR = Map.of(
        "A+",  List.of("A+", "A-", "O+", "O-"),
        "A-",  List.of("A-", "O-"),
        "B+",  List.of("B+", "B-", "O+", "O-"),
        "B-",  List.of("B-", "O-"),
        "AB+", List.of("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"),
        "AB-", List.of("A-", "B-", "AB-", "O-"),
        "O+",  List.of("O+", "O-"),
        "O-",  List.of("O-"));

    public static boolean isValid(String group) { return group != null && DONORS_FOR.containsKey(group); }
    public static List<String> donorsFor(String patientGroup) { return DONORS_FOR.get(patientGroup); }
}
