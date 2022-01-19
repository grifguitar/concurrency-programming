import java.util.concurrent.locks.ReentrantLock

/**
 * Bank implementation.
 *
 * @author Khlytin Grigoriy
 */
class BankImpl(n: Int) : Bank {
    private val accounts: Array<Account> = Array(n) { Account() }

    override val numberOfAccounts: Int
        get() = accounts.size

    override fun getAmount(index: Int): Long {
        accounts[index].lock.lock()
        try {
            return accounts[index].amount
        } finally {
            accounts[index].lock.unlock()
        }
    }

    private fun calcTotalAmount(index: Int): Long {
        if (index == accounts.size) {
            var sum: Long = 0
            for (account in accounts) {
                sum += account.amount
            }
            return sum
        }

        accounts[index].lock.lock()
        try {
            return calcTotalAmount(index + 1)
        } finally {
            accounts[index].lock.unlock()
        }
    }

    override val totalAmount: Long
        get() {
            return calcTotalAmount(0)
        }

    override fun deposit(index: Int, amount: Long): Long {
        require(amount > 0) { "Invalid amount: $amount" }
        accounts[index].lock.lock()
        try {
            val account = accounts[index]
            check(!(amount > Bank.MAX_AMOUNT || account.amount + amount > Bank.MAX_AMOUNT)) { "Overflow" }
            account.amount += amount
            return account.amount
        } finally {
            accounts[index].lock.unlock()
        }
    }

    override fun withdraw(index: Int, amount: Long): Long {
        require(amount > 0) { "Invalid amount: $amount" }
        accounts[index].lock.lock()
        try {
            val account = accounts[index]
            check(account.amount - amount >= 0) { "Underflow" }
            account.amount -= amount
            return account.amount
        } finally {
            accounts[index].lock.unlock()
        }
    }

    private fun calcTransfer(fromIndex: Int, toIndex: Int, amount: Long) {
        val from = accounts[fromIndex]
        val to = accounts[toIndex]
        check(amount <= from.amount) { "Underflow" }
        check(!(amount > Bank.MAX_AMOUNT || to.amount + amount > Bank.MAX_AMOUNT)) { "Overflow" }
        from.amount -= amount
        to.amount += amount
    }

    override fun transfer(fromIndex: Int, toIndex: Int, amount: Long) {
        require(amount > 0) { "Invalid amount: $amount" }
        require(fromIndex != toIndex) { "fromIndex == toIndex" }

        if (fromIndex < toIndex) {

            accounts[fromIndex].lock.lock()
            try {
                accounts[toIndex].lock.lock()
                try {
                    calcTransfer(fromIndex, toIndex, amount)
                } finally {
                    accounts[toIndex].lock.unlock()
                }
            } finally {
                accounts[fromIndex].lock.unlock()
            }

        } else {

            accounts[toIndex].lock.lock()
            try {
                accounts[fromIndex].lock.lock()
                try {
                    calcTransfer(fromIndex, toIndex, amount)
                } finally {
                    accounts[fromIndex].lock.unlock()
                }
            } finally {
                accounts[toIndex].lock.unlock()
            }

        }
    }

    /**
     * Private account data structure.
     */
    class Account {
        /**
         * Amount of funds in this account.
         */
        var amount: Long = 0
        val lock: ReentrantLock = ReentrantLock()
    }
}